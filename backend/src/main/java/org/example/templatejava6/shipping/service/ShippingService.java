package org.example.templatejava6.shipping.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.shipping.client.GhnClient;
import org.example.templatejava6.shipping.config.GhnProperties;
import org.example.templatejava6.shipping.model.request.CreateShippingOrderRequest;
import org.example.templatejava6.shipping.model.request.ShippingFeeRequest;
import org.example.templatejava6.shipping.model.response.CreateShippingOrderResponse;
import org.example.templatejava6.shipping.model.response.GhnDistrictResponse;
import org.example.templatejava6.shipping.model.response.GhnProvinceResponse;
import org.example.templatejava6.shipping.model.response.GhnWardResponse;
import org.example.templatejava6.shipping.model.response.ShippingFeeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dich vu tich hop Giao Hang Nhanh (GHN): lay danh muc dia chi, tinh phi va tao van don.
 * Cac thao tac doc danh muc se nem loi neu chua cau hinh; rieng tinh phi se tu dong
 * dung phi mac dinh khi GHN chua san sang de luong dat hang khong bi gian doan.
 */
@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);
    private static final Collator VI_COLLATOR = Collator.getInstance(new Locale("vi", "VN"));

    private final GhnClient ghnClient;
    private final GhnProperties properties;

    public ShippingService(GhnClient ghnClient, GhnProperties properties) {
        this.ghnClient = ghnClient;
        this.properties = properties;
    }

    public List<GhnProvinceResponse> getProvinces() {
        requireConfigured();
        JsonNode data = dataOf(call(() -> ghnClient.get("/master-data/province"), "tỉnh/thành"));
        List<GhnProvinceResponse> result = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                Integer id = intOrNull(node, "ProvinceID");
                String name = text(node, "ProvinceName");
                if (id != null && name != null) {
                    result.add(new GhnProvinceResponse(id, name));
                }
            }
        }
        result.sort(Comparator.comparing(GhnProvinceResponse::getProvinceName, VI_COLLATOR));
        return result;
    }

    public List<GhnDistrictResponse> getDistricts(Integer provinceId) {
        requireConfigured();
        if (provinceId == null) {
            throw new ApiException("Thiếu mã tỉnh/thành.", "VALIDATION_ERROR");
        }
        JsonNode data = dataOf(
                call(() -> ghnClient.post("/master-data/district", Map.of("province_id", provinceId)), "quận/huyện"));
        List<GhnDistrictResponse> result = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                Integer id = intOrNull(node, "DistrictID");
                String name = text(node, "DistrictName");
                if (id != null && name != null) {
                    result.add(new GhnDistrictResponse(id, name, intOrNull(node, "ProvinceID")));
                }
            }
        }
        result.sort(Comparator.comparing(GhnDistrictResponse::getDistrictName, VI_COLLATOR));
        return result;
    }

    public List<GhnWardResponse> getWards(Integer districtId) {
        requireConfigured();
        if (districtId == null) {
            throw new ApiException("Thiếu mã quận/huyện.", "VALIDATION_ERROR");
        }
        JsonNode data = dataOf(
                call(() -> ghnClient.post("/master-data/ward", Map.of("district_id", districtId)), "phường/xã"));
        List<GhnWardResponse> result = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                String code = text(node, "WardCode");
                String name = text(node, "WardName");
                if (code != null && name != null) {
                    result.add(new GhnWardResponse(code, name, intOrNull(node, "DistrictID")));
                }
            }
        }
        result.sort(Comparator.comparing(GhnWardResponse::getWardName, VI_COLLATOR));
        return result;
    }

    public ShippingFeeResponse calcFee(ShippingFeeRequest request) {
        long fallback = properties.getFallbackFee() != null ? properties.getFallbackFee() : 0L;
        if (!properties.isFeeConfigured()) {
            return ShippingFeeResponse.fallback(fallback,
                    "Chưa cấu hình đầy đủ GHN (ShopId / kho gửi), áp dụng phí mặc định.");
        }
        if (request == null || request.getToDistrictId() == null
                || request.getToWardCode() == null || request.getToWardCode().isBlank()) {
            return ShippingFeeResponse.fallback(fallback, "Thiếu địa chỉ nhận hàng, áp dụng phí mặc định.");
        }

        // Moi tuyen (from-district -> to-district) co the ho tro cac goi dich vu khac nhau,
        // vi vay phai hoi GHN service_id hop le truoc thay vi cố dinh service_type_id.
        Integer serviceId = resolveServiceId(request.getToDistrictId());

        Map<String, Object> body = new LinkedHashMap<>();
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id", properties.getServiceTypeId());
        }
        body.put("from_district_id", properties.getFromDistrictId());
        if (properties.getFromWardCode() != null && !properties.getFromWardCode().isBlank()) {
            body.put("from_ward_code", properties.getFromWardCode());
        }
        body.put("to_district_id", request.getToDistrictId());
        body.put("to_ward_code", request.getToWardCode());
        body.put("weight", request.getWeight() != null ? request.getWeight() : properties.getDefaultWeight());
        body.put("length", properties.getDefaultLength());
        body.put("width", properties.getDefaultWidth());
        body.put("height", properties.getDefaultHeight());
        if (request.getInsuranceValue() != null && request.getInsuranceValue() > 0) {
            body.put("insurance_value", request.getInsuranceValue());
        }

        try {
            JsonNode response = ghnClient.postWithShop("/v2/shipping-order/fee", body);
            JsonNode data = response != null ? response.path("data") : null;
            if (data != null && data.hasNonNull("total")) {
                return ShippingFeeResponse.fromGhn(data.path("total").asLong());
            }
            return ShippingFeeResponse.fallback(fallback, "GHN không trả về phí, áp dụng phí mặc định.");
        } catch (RestClientException ex) {
            log.warn("GHN tính phí thất bại (to_district={}, to_ward={}, service_id={}): {}",
                    request.getToDistrictId(), request.getToWardCode(), serviceId, ghnError(ex));
            return ShippingFeeResponse.fallback(fallback, "Không tính được phí GHN, áp dụng phí mặc định.");
        }
    }

    /**
     * Goi GHN available-services de lay service_id phu hop cho tuyen tu kho gui den quan/huyen nhan.
     * Uu tien service_type_id da cau hinh, neu khong co thi lay goi dau tien.
     */
    private Integer resolveServiceId(Integer toDistrictId) {
        Integer shopId = parseShopId();
        if (shopId == null) {
            return null;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("shop_id", shopId);
        body.put("from_district", properties.getFromDistrictId());
        body.put("to_district", toDistrictId);
        try {
            JsonNode response = ghnClient.post("/v2/shipping-order/available-services", body);
            JsonNode data = response != null ? response.path("data") : null;
            if (data == null || !data.isArray() || data.isEmpty()) {
                log.warn("GHN available-services rỗng cho tuyến {} -> {}", properties.getFromDistrictId(), toDistrictId);
                return null;
            }
            Integer preferredType = properties.getServiceTypeId();
            Integer firstServiceId = null;
            for (JsonNode node : data) {
                Integer serviceId = intOrNull(node, "service_id");
                if (serviceId == null) {
                    continue;
                }
                if (firstServiceId == null) {
                    firstServiceId = serviceId;
                }
                if (preferredType != null && preferredType.equals(intOrNull(node, "service_type_id"))) {
                    return serviceId;
                }
            }
            return firstServiceId;
        } catch (RestClientException ex) {
            log.warn("GHN available-services thất bại cho tuyến {} -> {}: {}",
                    properties.getFromDistrictId(), toDistrictId, ghnError(ex));
            return null;
        }
    }

    private Integer parseShopId() {
        String shopId = properties.getShopId();
        if (shopId == null || shopId.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(shopId.trim());
        } catch (NumberFormatException ex) {
            log.warn("ghn.shop-id không hợp lệ: {}", shopId);
            return null;
        }
    }

    private String ghnError(RestClientException ex) {
        if (ex instanceof RestClientResponseException re) {
            return re.getStatusCode() + " " + re.getResponseBodyAsString();
        }
        return ex.getMessage();
    }

    /** Trich thong bao loi tu body GHN (truong hop HTTP 200 nhung khong co order_code). */
    private String ghnMessage(JsonNode response) {
        if (response == null || response.isMissingNode() || response.isNull()) {
            return "không có phản hồi từ GHN.";
        }
        String message = text(response, "message");
        if (message == null) {
            message = text(response, "message_display");
        }
        Integer code = intOrNull(response, "code");
        if (message != null) {
            return code != null ? "code=" + code + ", message=" + message : message;
        }
        return response.toString();
    }

    public CreateShippingOrderResponse createOrder(CreateShippingOrderRequest request) {
        if (!properties.isFeeConfigured()) {
            throw new ApiException("Chưa cấu hình ShopId / kho gửi của GHN.", "GHN_NOT_CONFIGURED");
        }

        Integer serviceId = resolveServiceId(request.getToDistrictId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_type_id", 2); // nguoi nhan tra phi
        body.put("required_note", "KHONGCHOXEMHANG");
        body.put("to_name", request.getToName());
        body.put("to_phone", request.getToPhone());
        body.put("to_address", request.getToAddress());
        body.put("to_ward_code", request.getToWardCode());
        body.put("to_district_id", request.getToDistrictId());
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id", properties.getServiceTypeId());
        }
        body.put("weight", request.getWeight() != null ? request.getWeight() : properties.getDefaultWeight());
        body.put("length", properties.getDefaultLength());
        body.put("width", properties.getDefaultWidth());
        body.put("height", properties.getDefaultHeight());
        if (request.getCodAmount() != null && request.getCodAmount() > 0) {
            body.put("cod_amount", request.getCodAmount());
        }
        if (request.getInsuranceValue() != null && request.getInsuranceValue() > 0) {
            body.put("insurance_value", request.getInsuranceValue());
        }
        body.put("items", buildItems(request));

        try {
            JsonNode response = ghnClient.postWithShop("/v2/shipping-order/create", body);
            JsonNode data = response != null ? response.path("data") : null;
            if (data == null || data.isMissingNode() || !data.hasNonNull("order_code")) {
                String chiTiet = ghnMessage(response);
                log.warn("GHN không trả về mã vận đơn (to_district={}, service_id={}): {}",
                        request.getToDistrictId(), serviceId, chiTiet);
                throw new ApiException("GHN không trả về mã vận đơn. Phản hồi GHN: " + chiTiet, "GHN_ERROR");
            }
            return new CreateShippingOrderResponse(
                    data.path("order_code").asText(),
                    data.hasNonNull("total_fee") ? data.path("total_fee").asLong() : null,
                    text(data, "expected_delivery_time"));
        } catch (RestClientException ex) {
            String chiTiet = ghnError(ex);
            log.warn("GHN tạo vận đơn thất bại (to_district={}, service_id={}): {}",
                    request.getToDistrictId(), serviceId, chiTiet);
            throw new ApiException("Không tạo được vận đơn GHN. Lỗi GHN: " + chiTiet, "GHN_ERROR");
        }
    }

    private List<Map<String, Object>> buildItems(CreateShippingOrderRequest request) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreateShippingOrderRequest.Item item : request.getItems()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", item.getName() != null ? item.getName() : "Sản phẩm");
                map.put("quantity", item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1);
                map.put("weight", item.getWeight() != null ? item.getWeight() : properties.getDefaultWeight());
                items.add(map);
            }
            return items;
        }
        Map<String, Object> single = new LinkedHashMap<>();
        single.put("name", "Đơn hàng");
        single.put("quantity", 1);
        single.put("weight", request.getWeight() != null ? request.getWeight() : properties.getDefaultWeight());
        items.add(single);
        return items;
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new ApiException("Chưa cấu hình token GHN.", "GHN_NOT_CONFIGURED");
        }
    }

    private JsonNode call(GhnCall supplier, String label) {
        try {
            return supplier.execute();
        } catch (RestClientException ex) {
            throw new ApiException("Không lấy được danh sách " + label + " từ GHN.", "GHN_ERROR");
        }
    }

    private JsonNode dataOf(JsonNode response) {
        JsonNode data = response != null ? response.path("data") : null;
        if (data == null || data.isMissingNode() || data.isNull()) {
            throw new ApiException("GHN không trả về dữ liệu.", "GHN_ERROR");
        }
        return data;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText(null);
    }

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    @FunctionalInterface
    private interface GhnCall {
        JsonNode execute() throws RestClientException;
    }
}
