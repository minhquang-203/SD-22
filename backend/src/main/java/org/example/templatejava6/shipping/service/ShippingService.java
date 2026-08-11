package org.example.templatejava6.shipping.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.shipping.client.GhnClient;
import org.example.templatejava6.shipping.config.GhnProperties;
import org.example.templatejava6.shipping.model.request.CreateShippingOrderRequest;
import org.example.templatejava6.shipping.model.request.ReturnShippingOrderRequest;
import org.example.templatejava6.shipping.model.request.ShippingFeeRequest;
import org.example.templatejava6.shipping.model.response.CreateShippingOrderResponse;
import org.example.templatejava6.shipping.model.response.GhnPickShiftResponse;
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
 * Dich vu tich hop Giao Hang Nhanh (GHN): lay danh muc dia chi 2 cap (v3), tinh phi va tao van don.
 */
@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);
    private static final Collator VI_COLLATOR = Collator.getInstance(new Locale("vi", "VN"));
    private static final int PAGE_SIZE = 200;

    private final GhnClient ghnClient;
    private final GhnProperties properties;

    public ShippingService(GhnClient ghnClient, GhnProperties properties) {
        this.ghnClient = ghnClient;
        this.properties = properties;
    }

    /** Tỉnh/thành đơn vị hành chính mới (GHN v3). */
    public List<GhnProvinceResponse> getProvinces() {
        requireConfigured();
        List<GhnProvinceResponse> result = new ArrayList<>();
        int offset = 0;
        while (true) {
            Map<String, Object> body = Map.of("offset", offset, "limit", PAGE_SIZE);
            JsonNode data = dataOf(call(
                    () -> ghnClient.post("/v3/master-data/province/all", body), "tỉnh/thành"));
            if (!data.isArray() || data.isEmpty()) {
                break;
            }
            int pageCount = 0;
            for (JsonNode node : data) {
                Integer id = intOrNull(node, "_id");
                String name = text(node, "name");
                if (id != null && name != null) {
                    result.add(new GhnProvinceResponse(id, name));
                    pageCount++;
                }
            }
            offset += pageCount;
            if (pageCount < PAGE_SIZE) {
                break;
            }
        }
        result.sort(Comparator.comparing(GhnProvinceResponse::getProvinceName, VI_COLLATOR));
        return result;
    }

    /** Phường/xã theo tỉnh — địa chỉ 2 cấp (GHN v3). */
    public List<GhnWardResponse> getWardsByProvince(Integer provinceId) {
        requireConfigured();
        if (provinceId == null) {
            throw new ApiException("Thiếu mã tỉnh/thành.", "VALIDATION_ERROR");
        }
        List<GhnWardResponse> result = new ArrayList<>();
        int offset = 0;
        while (true) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("province_id", provinceId);
            body.put("offset", offset);
            body.put("limit", PAGE_SIZE);
            JsonNode data = dataOf(call(
                    () -> ghnClient.post("/v3/master-data/ward/all-by-province-id", body), "phường/xã"));
            if (!data.isArray() || data.isEmpty()) {
                break;
            }
            int pageCount = 0;
            for (JsonNode node : data) {
                Integer id = intOrNull(node, "_id");
                String name = text(node, "name");
                if (id != null && name != null) {
                    result.add(new GhnWardResponse(String.valueOf(id), name, provinceId));
                    pageCount++;
                }
            }
            offset += pageCount;
            if (pageCount < PAGE_SIZE) {
                break;
            }
        }
        result.sort(Comparator.comparing(GhnWardResponse::getWardName, VI_COLLATOR));
        return result;
    }

    /**
     * Danh sach ca lay hang GHN con hieu luc (API /v2/shift/date).
     */
    public List<GhnPickShiftResponse> getPickShifts() {
        requireConfigured();
        JsonNode data = dataOf(call(() -> ghnClient.get("/v2/shift/date"), "ca lấy hàng"));
        List<GhnPickShiftResponse> result = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                Integer id = intOrNull(node, "id");
                if (id == null) {
                    continue;
                }
                result.add(new GhnPickShiftResponse(
                        id,
                        text(node, "title"),
                        longOrNull(node, "from_time"),
                        longOrNull(node, "to_time")));
            }
        }
        result.sort(Comparator.comparing(
                GhnPickShiftResponse::getFromTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    public ShippingFeeResponse calcFee(ShippingFeeRequest request) {
        long fallback = properties.getFallbackFee() != null ? properties.getFallbackFee() : 0L;
        if (!properties.isFeeConfigured()) {
            return ShippingFeeResponse.fallback(fallback,
                    "Chưa cấu hình đầy đủ GHN (ShopId / kho gửi), áp dụng phí mặc định.");
        }
        if (request == null) {
            return ShippingFeeResponse.fallback(fallback, "Thiếu địa chỉ nhận hàng, áp dụng phí mặc định.");
        }

        Integer toWardIdV2 = resolveToWardIdV2(request);
        boolean useNewToAddress = toWardIdV2 != null;
        if (!useNewToAddress
                && (request.getToDistrictId() == null
                || request.getToWardCode() == null || request.getToWardCode().isBlank())) {
            return ShippingFeeResponse.fallback(fallback, "Thiếu địa chỉ nhận hàng, áp dụng phí mặc định.");
        }

        String toAddressV2 = request.getToAddressV2();
        if (useNewToAddress && (toAddressV2 == null || toAddressV2.isBlank())) {
            toAddressV2 = "Địa chỉ nhận hàng";
        }

        Integer serviceId = useNewToAddress
                ? resolveServiceIdForNewToAddress(toWardIdV2, toAddressV2)
                : resolveServiceIdLegacy(request.getToDistrictId());

        Map<String, Object> body = new LinkedHashMap<>();
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id", properties.getServiceTypeId());
        }
        // Kho gửi: giữ địa chỉ cũ
        body.put("is_new_from_address", false);
        body.put("from_district_id", properties.getFromDistrictId());
        if (properties.getFromWardCode() != null && !properties.getFromWardCode().isBlank()) {
            body.put("from_ward_code", properties.getFromWardCode());
        }

        if (useNewToAddress) {
            body.put("is_new_to_address", true);
            body.put("to_ward_id_v2", toWardIdV2);
            body.put("to_address_v2", toAddressV2);
        } else {
            body.put("is_new_to_address", false);
            body.put("to_district_id", request.getToDistrictId());
            body.put("to_ward_code", request.getToWardCode());
        }

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
            log.warn("GHN tính phí thất bại (new={}, to_ward_id_v2={}, to_district={}, service_id={}): {}",
                    useNewToAddress, toWardIdV2, request.getToDistrictId(), serviceId, ghnError(ex));
            return ShippingFeeResponse.fallback(fallback, "Không tính được phí GHN, áp dụng phí mặc định.");
        }
    }

    private Integer resolveToWardIdV2(ShippingFeeRequest request) {
        if (request.getToWardIdV2() != null) {
            return request.getToWardIdV2();
        }
        // Frontend có thể gửi ward id mới trong toWardCode (string số >= 1000000)
        if (request.getToWardCode() != null && !request.getToWardCode().isBlank()) {
            try {
                int id = Integer.parseInt(request.getToWardCode().trim());
                if (id >= 1_000_000) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
                // ward code cũ dạng chuỗi alphanumeric
            }
        }
        return null;
    }

    private Integer resolveServiceIdForNewToAddress(Integer toWardIdV2, String toAddressV2) {
        Integer shopId = parseShopId();
        if (shopId == null || toWardIdV2 == null) {
            return null;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("shop_id", shopId);
        body.put("is_new_from_address", false);
        body.put("from_district", properties.getFromDistrictId());
        body.put("is_new_to_address", true);
        body.put("to_ward_id_v2", toWardIdV2);
        body.put("to_address_v2", toAddressV2 != null ? toAddressV2 : "");
        return pickServiceId(body, "new-to ward=" + toWardIdV2);
    }

    private Integer resolveServiceIdLegacy(Integer toDistrictId) {
        return resolveServiceId(properties.getFromDistrictId(), toDistrictId);
    }

    private Integer resolveServiceId(Integer fromDistrictId, Integer toDistrictId) {
        Integer shopId = parseShopId();
        if (shopId == null) {
            return null;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("shop_id", shopId);
        body.put("from_district", fromDistrictId);
        body.put("to_district", toDistrictId);
        return pickServiceId(body, fromDistrictId + " -> " + toDistrictId);
    }

    private Integer pickServiceId(Map<String, Object> body, String routeLabel) {
        try {
            JsonNode response = ghnClient.post("/v2/shipping-order/available-services", body);
            JsonNode data = response != null ? response.path("data") : null;
            if (data == null || !data.isArray() || data.isEmpty()) {
                log.warn("GHN available-services rỗng cho tuyến {}", routeLabel);
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
            log.warn("GHN available-services thất bại cho tuyến {}: {}", routeLabel, ghnError(ex));
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

        boolean isNewTo = Boolean.TRUE.equals(request.getIsNewToAddress())
                || looksLikeNewWardCode(request.getToWardCode());

        Integer toWardIdV2 = null;
        if (isNewTo && request.getToWardCode() != null) {
            try {
                toWardIdV2 = Integer.parseInt(request.getToWardCode().trim());
            } catch (NumberFormatException ignored) {
                // dùng ward name
            }
        }

        Integer serviceId;
        if (isNewTo && toWardIdV2 != null) {
            serviceId = resolveServiceIdForNewToAddress(toWardIdV2, request.getToAddress());
        } else if (!isNewTo && request.getToDistrictId() != null) {
            serviceId = resolveServiceIdLegacy(request.getToDistrictId());
        } else {
            serviceId = null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_type_id", 2);
        body.put("required_note", "KHONGCHOXEMHANG");
        body.put("to_name", request.getToName());
        body.put("to_phone", request.getToPhone());
        body.put("to_address", request.getToAddress());

        // Kho gửi cũ
        body.put("is_new_from_address", false);

        if (isNewTo) {
            body.put("is_new_to_address", true);
            if (request.getToProvinceName() != null && !request.getToProvinceName().isBlank()) {
                body.put("to_province_name", request.getToProvinceName().trim());
            }
            if (request.getToWardName() != null && !request.getToWardName().isBlank()) {
                body.put("to_ward_name", request.getToWardName().trim());
            }
            if (request.getToWardCode() != null && !request.getToWardCode().isBlank()) {
                body.put("to_ward_code", request.getToWardCode().trim());
            }
        } else {
            body.put("is_new_to_address", false);
            body.put("to_ward_code", request.getToWardCode());
            body.put("to_district_id", request.getToDistrictId());
        }

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
                log.warn("GHN không trả về mã vận đơn (new={}, ward={}, service_id={}): {}",
                        isNewTo, request.getToWardCode(), serviceId, chiTiet);
                throw new ApiException("GHN không trả về mã vận đơn. Phản hồi GHN: " + chiTiet, "GHN_ERROR");
            }
            return new CreateShippingOrderResponse(
                    data.path("order_code").asText(),
                    data.hasNonNull("total_fee") ? data.path("total_fee").asLong() : null,
                    text(data, "expected_delivery_time"));
        } catch (RestClientException ex) {
            String chiTiet = ghnError(ex);
            log.warn("GHN tạo vận đơn thất bại (new={}, ward={}, service_id={}): {}",
                    isNewTo, request.getToWardCode(), serviceId, chiTiet);
            throw new ApiException("Không tạo được vận đơn GHN. Lỗi GHN: " + chiTiet, "GHN_ERROR");
        }
    }

    public static boolean looksLikeNewWardCode(String wardCode) {
        if (wardCode == null || wardCode.isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(wardCode.trim()) >= 1_000_000;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Tao van don hoan tra hang: nguoi gui la khach hang (from_*), nguoi nhan la shop.
     * Neu dia chi khach la 2 cap moi thi dung is_new_from_address.
     */
    public CreateShippingOrderResponse createReturnOrder(ReturnShippingOrderRequest request) {
        if (!properties.isFeeConfigured()) {
            throw new ApiException("Chưa cấu hình ShopId / kho gửi của GHN.", "GHN_NOT_CONFIGURED");
        }
        boolean newFrom = looksLikeNewWardCode(request.getFromWardCode());
        if (!newFrom && (request.getFromDistrictId() == null
                || request.getFromWardCode() == null || request.getFromWardCode().isBlank())) {
            throw new ApiException(
                    "Thiếu quận/huyện hoặc phường/xã của địa chỉ lấy hàng trả.", "GHN_MISSING_ADDRESS");
        }
        if (newFrom && (request.getFromWardCode() == null || request.getFromWardCode().isBlank())) {
            throw new ApiException("Thiếu phường/xã của địa chỉ lấy hàng trả.", "GHN_MISSING_ADDRESS");
        }

        Integer fromWardIdV2 = null;
        if (newFrom) {
            try {
                fromWardIdV2 = Integer.parseInt(request.getFromWardCode().trim());
            } catch (NumberFormatException ex) {
                throw new ApiException("Mã phường/xã trả hàng không hợp lệ.", "GHN_MISSING_ADDRESS");
            }
        }

        Integer serviceId;
        if (newFrom && fromWardIdV2 != null) {
            // available-services: from = new customer, to = old shop
            Integer shopId = parseShopId();
            if (shopId == null) {
                serviceId = null;
            } else {
                Map<String, Object> svcBody = new LinkedHashMap<>();
                svcBody.put("shop_id", shopId);
                svcBody.put("is_new_from_address", true);
                svcBody.put("from_ward_id_v2", fromWardIdV2);
                svcBody.put("from_address_v2", request.getFromAddress());
                svcBody.put("is_new_to_address", false);
                svcBody.put("to_district", properties.getFromDistrictId());
                serviceId = pickServiceId(svcBody, "return new-from -> shop");
            }
        } else {
            serviceId = resolveServiceId(request.getFromDistrictId(), properties.getFromDistrictId());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_type_id", 1);
        body.put("required_note", "CHOXEMHANGKHONGTHU");
        body.put("from_name", request.getFromName());
        body.put("from_phone", request.getFromPhone());
        body.put("from_address", request.getFromAddress());
        if (newFrom) {
            body.put("is_new_from_address", true);
            body.put("from_ward_code", request.getFromWardCode());
            if (request.getFromProvinceName() != null && !request.getFromProvinceName().isBlank()) {
                body.put("from_province_name", request.getFromProvinceName().trim());
            }
            if (request.getFromWardName() != null && !request.getFromWardName().isBlank()) {
                body.put("from_ward_name", request.getFromWardName().trim());
            }
        } else {
            body.put("is_new_from_address", false);
            body.put("from_ward_code", request.getFromWardCode());
            body.put("from_district_id", request.getFromDistrictId());
        }

        body.put("is_new_to_address", false);
        body.put("to_name", orElse(properties.getShopName(), "SUNOVA Shop"));
        body.put("to_phone", orElse(properties.getShopPhone(), "0900000000"));
        body.put("to_address", orElse(properties.getShopAddress(), "Kho SUNOVA"));
        body.put("to_ward_code", properties.getFromWardCode());
        body.put("to_district_id", properties.getFromDistrictId());
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id", properties.getServiceTypeId());
        }
        body.put("weight", request.getWeight() != null ? request.getWeight() : properties.getDefaultWeight());
        body.put("length", properties.getDefaultLength());
        body.put("width", properties.getDefaultWidth());
        body.put("height", properties.getDefaultHeight());
        if (request.getInsuranceValue() != null && request.getInsuranceValue() > 0) {
            body.put("insurance_value", request.getInsuranceValue());
        }
        if (request.getPickShiftId() != null) {
            body.put("pick_shift", List.of(request.getPickShiftId()));
        }
        body.put("items", buildReturnItems(request));

        try {
            JsonNode response = ghnClient.postWithShop("/v2/shipping-order/create", body);
            JsonNode data = response != null ? response.path("data") : null;
            if (data == null || data.isMissingNode() || !data.hasNonNull("order_code")) {
                String chiTiet = ghnMessage(response);
                log.warn("GHN không trả về mã vận đơn hoàn trả (from_ward={}): {}",
                        request.getFromWardCode(), chiTiet);
                throw new ApiException("GHN không trả về mã vận đơn hoàn trả. Phản hồi GHN: " + chiTiet, "GHN_ERROR");
            }
            return new CreateShippingOrderResponse(
                    data.path("order_code").asText(),
                    data.hasNonNull("total_fee") ? data.path("total_fee").asLong() : null,
                    text(data, "expected_delivery_time"));
        } catch (RestClientException ex) {
            String chiTiet = ghnError(ex);
            log.warn("GHN tạo vận đơn hoàn trả thất bại (from_ward={}): {}",
                    request.getFromWardCode(), chiTiet);
            throw new ApiException("Không tạo được vận đơn hoàn trả GHN. Lỗi GHN: " + chiTiet, "GHN_ERROR");
        }
    }

    private List<Map<String, Object>> buildReturnItems(ReturnShippingOrderRequest request) {
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
        single.put("name", "Hàng hoàn trả");
        single.put("quantity", 1);
        single.put("weight", request.getWeight() != null ? request.getWeight() : properties.getDefaultWeight());
        items.add(single);
        return items;
    }

    private static String orElse(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
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

    private static Long longOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asLong();
    }

    @FunctionalInterface
    private interface GhnCall {
        JsonNode execute() throws RestClientException;
    }
}
