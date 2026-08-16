package org.example.templatejava6.shipping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper JSON = new ObjectMapper();

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
                    "Chưa cấu hình đầy đủ GHN (ShopId), áp dụng phí mặc định.");
        }
        if (request == null) {
            return ShippingFeeResponse.fallback(fallback, "Thiếu địa chỉ nhận hàng, áp dụng phí mặc định.");
        }

        boolean useNewToAddress = isNewNameAddress(request.getToProvinceName(), request.getToWardName())
                || looksLikeNewWardCode(request.getToWardCode());
        if (useNewToAddress && (isBlank(request.getToProvinceName()) || isBlank(request.getToWardName()))) {
            return ShippingFeeResponse.fallback(fallback,
                    "Thiếu tên tỉnh/thành hoặc phường/xã, áp dụng phí mặc định.");
        }
        if (!useNewToAddress
                && (request.getToDistrictId() == null
                || request.getToWardCode() == null || request.getToWardCode().isBlank())) {
            return ShippingFeeResponse.fallback(fallback, "Thiếu địa chỉ nhận hàng, áp dụng phí mặc định.");
        }

        Integer serviceId = !useNewToAddress && request.getToDistrictId() != null
                ? resolveServiceIdLegacy(request.getToDistrictId())
                : null;

        Map<String, Object> body = new LinkedHashMap<>();
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id", properties.getServiceTypeId());
        }
        putFromWarehouseFromConfig(body);

        if (useNewToAddress) {
            body.put("is_new_to_address", true);
            body.put("to_province_name", request.getToProvinceName().trim());
            body.put("to_ward_name", request.getToWardName().trim());
            body.put("to_address", firstNonBlank(request.getToAddressV2(), "Địa chỉ nhận hàng"));
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
            log.warn("GHN tính phí thất bại (new={}, tinh={}, phuong={}, to_district={}): {}",
                    useNewToAddress, request.getToProvinceName(), request.getToWardName(),
                    request.getToDistrictId(), ghnError(ex));
            return ShippingFeeResponse.fallback(fallback, "Không tính được phí GHN, áp dụng phí mặc định.");
        }
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
            String body = re.getResponseBodyAsString();
            String extracted = extractGhnMessage(body);
            if (extracted != null && !extracted.isBlank()) {
                return extracted;
            }
            return re.getStatusCode() + " " + body;
        }
        return ex.getMessage();
    }

    private static String extractGhnMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = JSON.readTree(body);
            String message = text(node, "message_display");
            if (message == null) {
                message = text(node, "message");
            }
            if (message == null) {
                message = text(node, "code_message");
            }
            return message;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String ghnCreateFailMessage(String chiTiet) {
        String detail = chiTiet != null ? chiTiet.trim() : "";
        String lower = detail.toLowerCase();
        if (lower.contains("kho") || lower.contains("warehouse")) {
            return "Không tạo được vận đơn GHN vì không lấy được thông tin kho gửi. "
                    + "Khai báo GHN_FROM_WARD_NAME / GHN_FROM_PROVINCE_NAME (địa chỉ 2 cấp) "
                    + "hoặc kiểm tra ShopId trên GHN. "
                    + (detail.isBlank() ? "" : "Chi tiết: " + detail);
        }
        if (detail.isBlank()) {
            return "Không tạo được vận đơn GHN.";
        }
        return "Không tạo được vận đơn GHN. " + detail;
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
                || isNewNameAddress(request.getToProvinceName(), request.getToWardName())
                || looksLikeNewWardCode(request.getToWardCode());

        // available-services chi nhan from_district/to_district (ID cu). Voi dia chi 2 cap
        // khong co district nen bo qua, dung service_type_id.
        Integer serviceId = !isNewTo && request.getToDistrictId() != null
                ? resolveServiceIdLegacy(request.getToDistrictId())
                : null;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_type_id", 2);
        body.put("required_note", "KHONGCHOXEMHANG");
        body.put("to_name", request.getToName());
        body.put("to_phone", request.getToPhone());
        body.put("to_address", request.getToAddress());

        putFromWarehouse(body);

        if (isNewTo) {
            // Dia chi 2 cap (GHN docs id=122): chi gui TEN, khong gui to_ward_code / to_district_id.
            if (isBlank(request.getToProvinceName()) || isBlank(request.getToWardName())) {
                throw new ApiException(
                        "Thiếu tên tỉnh/thành hoặc phường/xã người nhận (địa chỉ 2 cấp).",
                        "GHN_MISSING_ADDRESS");
            }
            body.put("is_new_to_address", true);
            body.put("to_province_name", request.getToProvinceName().trim());
            body.put("to_ward_name", request.getToWardName().trim());
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
                log.warn("GHN không trả về mã vận đơn (new={}, request={}): {}", isNewTo, body, chiTiet);
                throw new ApiException("GHN không trả về mã vận đơn. Phản hồi GHN: " + chiTiet, "GHN_ERROR");
            }
            return new CreateShippingOrderResponse(
                    data.path("order_code").asText(),
                    data.hasNonNull("total_fee") ? data.path("total_fee").asLong() : null,
                    text(data, "expected_delivery_time"));
        } catch (RestClientException ex) {
            String chiTiet = ghnError(ex);
            log.warn("GHN tạo vận đơn thất bại (new={}, request={}): {}", isNewTo, body, chiTiet);
            throw new ApiException(ghnCreateFailMessage(chiTiet), "GHN_ERROR");
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

    private static boolean isNewNameAddress(String provinceName, String wardName) {
        return !isBlank(provinceName) && !isBlank(wardName);
    }

    /**
     * Tao van don hoan tra hang: nguoi gui la khach hang (from_*), nguoi nhan la shop.
     * Dia chi 2 cap: chi gui ten tinh/phuong, khong gui from_ward_code.
     */
    public CreateShippingOrderResponse createReturnOrder(ReturnShippingOrderRequest request) {
        if (!properties.isFeeConfigured()) {
            throw new ApiException("Chưa cấu hình ShopId / kho gửi của GHN.", "GHN_NOT_CONFIGURED");
        }
        boolean newFrom = isNewNameAddress(request.getFromProvinceName(), request.getFromWardName())
                || looksLikeNewWardCode(request.getFromWardCode());
        if (newFrom && (isBlank(request.getFromProvinceName()) || isBlank(request.getFromWardName()))) {
            throw new ApiException(
                    "Thiếu tên tỉnh/thành hoặc phường/xã của địa chỉ lấy hàng trả.", "GHN_MISSING_ADDRESS");
        }
        if (!newFrom && (request.getFromDistrictId() == null
                || request.getFromWardCode() == null || request.getFromWardCode().isBlank())) {
            throw new ApiException(
                    "Thiếu quận/huyện hoặc phường/xã của địa chỉ lấy hàng trả.", "GHN_MISSING_ADDRESS");
        }

        Integer serviceId = !newFrom
                ? resolveServiceId(request.getFromDistrictId(), properties.getFromDistrictId())
                : null;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_type_id", 1);
        body.put("required_note", "CHOXEMHANGKHONGTHU");
        body.put("from_name", request.getFromName());
        body.put("from_phone", request.getFromPhone());
        body.put("from_address", request.getFromAddress());
        if (newFrom) {
            body.put("is_new_from_address", true);
            body.put("from_province_name", request.getFromProvinceName().trim());
            body.put("from_ward_name", request.getFromWardName().trim());
        } else {
            body.put("is_new_from_address", false);
            body.put("from_ward_code", request.getFromWardCode());
            body.put("from_district_id", request.getFromDistrictId());
        }

        body.put("to_name", orElse(properties.getShopName(), "SUNOVA Shop"));
        body.put("to_phone", orElse(properties.getShopPhone(), "0900000000"));
        body.put("to_address", orElse(properties.getShopAddress(), "Kho SUNOVA"));
        putShopAsToAddress(body);
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
                log.warn("GHN không trả về mã vận đơn hoàn trả (from={}/{}): {}",
                        request.getFromProvinceName(), request.getFromWardName(), chiTiet);
                throw new ApiException("GHN không trả về mã vận đơn hoàn trả. Phản hồi GHN: " + chiTiet, "GHN_ERROR");
            }
            return new CreateShippingOrderResponse(
                    data.path("order_code").asText(),
                    data.hasNonNull("total_fee") ? data.path("total_fee").asLong() : null,
                    text(data, "expected_delivery_time"));
        } catch (RestClientException ex) {
            String chiTiet = ghnError(ex);
            log.warn("GHN tạo vận đơn hoàn trả thất bại (from={}/{}): {}",
                    request.getFromProvinceName(), request.getFromWardName(), chiTiet);
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

    /**
     * Kho gửi địa chỉ 2 cấp: chỉ gửi tên khi đã khai báo env (GHN_FROM_WARD_NAME /
     * GHN_FROM_PROVINCE_NAME). Không tự suy từ district_id / ward_code cũ — tên xã cũ
     * (vd. Xã Đồng Tiến) không còn trên master data 2 cấp, GHN sẽ báo ward not found.
     * Thiếu tên thì bỏ from_* để GHN lấy kho theo ShopId.
     */
    private void putFromWarehouse(Map<String, Object> body) {
        putFromWarehouseFromConfig(body);
        if (!body.containsKey("from_ward_name")) {
            log.info("Không gửi from_ward_name/from_province_name, GHN lấy kho theo ShopId {}.",
                    properties.getShopId());
        }
    }

    /** Tính phí: chỉ gửi tên kho nếu đã khai báo env, không gọi thêm API shop. */
    private void putFromWarehouseFromConfig(Map<String, Object> body) {
        if (isBlank(properties.getFromWardName()) || isBlank(properties.getFromProvinceName())) {
            return;
        }
        body.put("is_new_from_address", true);
        body.put("from_ward_name", properties.getFromWardName().trim());
        body.put("from_province_name", properties.getFromProvinceName().trim());
        if (!isBlank(properties.getShopName())) {
            body.put("from_name", properties.getShopName().trim());
        }
        if (!isBlank(properties.getShopPhone())) {
            body.put("from_phone", sanitizePhone(properties.getShopPhone()));
        }
        if (!isBlank(properties.getShopAddress())) {
            body.put("from_address", properties.getShopAddress().trim());
        }
    }

    /** Người nhận của đơn hoàn là kho shop — cùng hợp đồng tên 2 cấp từ env, hoặc ShopId. */
    private void putShopAsToAddress(Map<String, Object> body) {
        if (isBlank(properties.getFromWardName()) || isBlank(properties.getFromProvinceName())) {
            log.warn("Thiếu GHN_FROM_WARD_NAME / GHN_FROM_PROVINCE_NAME, để GHN lấy kho nhận theo ShopId {}.",
                    properties.getShopId());
            return;
        }
        body.put("is_new_to_address", true);
        body.put("to_ward_name", properties.getFromWardName().trim());
        body.put("to_province_name", properties.getFromProvinceName().trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String sanitizePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("\\D+", "");
        if (digits.startsWith("84") && digits.length() >= 11) {
            digits = "0" + digits.substring(2);
        }
        return digits.isBlank() ? null : digits;
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
