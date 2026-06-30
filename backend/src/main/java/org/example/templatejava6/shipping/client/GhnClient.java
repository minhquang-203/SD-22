package org.example.templatejava6.shipping.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.templatejava6.shipping.config.GhnProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Client cap thap goi truc tiep cac API cua GHN va tra ve JSON tho.
 * Phan xu ly nghiep vu duoc dat o {@code ShippingService}.
 */
@Component
public class GhnClient {

    private final RestTemplate restTemplate;
    private final GhnProperties properties;

    public GhnClient(RestTemplateBuilder builder, GhnProperties properties) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public JsonNode get(String path) {
        return exchange(HttpMethod.GET, path, null, false);
    }

    public JsonNode post(String path, Object body) {
        return exchange(HttpMethod.POST, path, body, false);
    }

    public JsonNode postWithShop(String path, Object body) {
        return exchange(HttpMethod.POST, path, body, true);
    }

    private JsonNode exchange(HttpMethod method, String path, Object body, boolean withShop) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", properties.getToken());
        if (withShop && properties.getShopId() != null && !properties.getShopId().isBlank()) {
            headers.set("ShopId", properties.getShopId());
        }
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                properties.getBaseUrl() + path, method, entity, JsonNode.class);
        return response.getBody();
    }
}
