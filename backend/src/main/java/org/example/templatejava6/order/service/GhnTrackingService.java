package org.example.templatejava6.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.templatejava6.order.model.response.GhnTrangThaiOptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GhnTrackingService {

    private static final String GHN_ORDER_DETAIL_URL =
            "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail";

    private static final List<String> ALL_STATUSES = List.of(
            "ready_to_pick",
            "picking",
            "cancel",
            "money_collect_picking",
            "picked",
            "storing",
            "transporting",
            "sorting",
            "delivering",
            "money_collect_delivering",
            "delivered",
            "delivery_fail",
            "waiting_to_return",
            "return",
            "return_transporting",
            "return_sorting",
            "returning",
            "return_fail",
            "returned",
            "exception",
            "damage",
            "lost"
    );

    private final RestTemplate restTemplate;
    private final String token;

    public GhnTrackingService(RestTemplateBuilder builder, @Value("${ghn.token:}") String token) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        this.token = token;
    }

    public Optional<TrackingInfo> track(String orderCode) {
        if (isBlank(token) || isBlank(orderCode)) {
            return Optional.empty();
        }

        String code = orderCode.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", token);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                Map.of("order_code", orderCode.trim()),
                headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(GHN_ORDER_DETAIL_URL, entity, JsonNode.class);
            JsonNode data = response.getBody() != null ? response.getBody().path("data") : null;
            if (data == null || data.isMissingNode() || data.isNull()) {
                return Optional.empty();
            }

            String status = text(data, "status");
            return Optional.of(new TrackingInfo(
                    textOrDefault(data, "order_code", orderCode.trim()),
                    status,
                    labelOf(status),
                    firstNonBlank(text(data, "leadtime"), text(data, "leadtime_order"))
            ));
        } catch (RestClientException ignored) {
            return Optional.empty();
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText(null);
    }

    private static String textOrDefault(JsonNode node, String field, String fallback) {
        String value = text(node, field);
        return isBlank(value) ? fallback : value;
    }

    private static String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static List<GhnTrangThaiOptionResponse> allStatusOptions() {
        return ALL_STATUSES.stream()
                .map(status -> new GhnTrangThaiOptionResponse(status, labelOf(status)))
                .toList();
    }

    public static String labelOf(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "ready_to_pick" -> "Chờ lấy hàng";
            case "picking" -> "Đang lấy hàng";
            case "money_collect_picking" -> "Đang thu tiền khi lấy hàng";
            case "picked" -> "Đã lấy hàng";
            case "storing" -> "Đang nhập kho";
            case "transporting", "sorting", "delivering" -> "Đang vận chuyển";
            case "money_collect_delivering" -> "Đang thu tiền khi giao hàng";
            case "delivered" -> "Đã giao";
            case "delivery_fail" -> "Giao hàng không thành công";
            case "waiting_to_return", "return" -> "Đang hoàn hàng";
            case "return_transporting", "return_sorting", "returning" -> "Đang chuyển hoàn";
            case "return_fail" -> "Hoàn hàng không thành công";
            case "returned" -> "Đã hoàn hàng";
            case "cancel" -> "Đã hủy giao hàng";
            case "exception", "damage", "lost" -> "Có sự cố vận chuyển";
            default -> status;
        };
    }

    public record TrackingInfo(
            String orderCode,
            String status,
            String statusLabel,
            String leadtime
    ) {
    }
}
