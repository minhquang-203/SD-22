package org.example.templatejava6.Uv.service;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.templatejava6.Uv.entity.WeatherData;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class WeatherService {

    /**
     * alias (đã bỏ dấu, lower) → tên gửi Open-Meteo geocoding.
     * Phần tử cuối mỗi hàng là query Latin ổn định.
     */
    private static final List<String[]> CITY_ALIASES = List.of(
            new String[]{"ha noi", "hanoi", "hn", "thanh pho ha noi", "Hanoi"},
            new String[]{"sai gon", "tp hcm", "tphcm", "hcm", "ho chi minh", "thanh pho ho chi minh",
                    "tp ho chi minh", "sg", "Ho Chi Minh City"},
            new String[]{"da nang", "thanh pho da nang", "dn", "Da Nang"},
            new String[]{"hai phong", "thanh pho hai phong", "Hai Phong"},
            new String[]{"can tho", "thanh pho can tho", "Can Tho"},
            new String[]{"hue", "thua thien hue", "Hue"},
            new String[]{"nha trang", "khanh hoa", "Nha Trang"},
            new String[]{"vung tau", "ba ria vung tau", "Vung Tau"},
            new String[]{"da lat", "lam dong", "Da Lat"},
            new String[]{"quy nhon", "binh dinh", "Quy Nhon"},
            new String[]{"bien hoa", "dong nai", "Bien Hoa"},
            new String[]{"buon ma thuot", "dak lak", "Buon Ma Thuot"}
    );

    public WeatherData getWeatherData(String city) {
        WeatherData data = new WeatherData();
        RestTemplate restTemplate = new RestTemplate();
        String queryCity = normalizeCityForGeocode(city);

        try {
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + java.net.URLEncoder.encode(queryCity, java.nio.charset.StandardCharsets.UTF_8)
                    + "&count=1&language=en";
            JsonNode geoResponse = restTemplate.getForObject(geoUrl, JsonNode.class);

            if (geoResponse != null && geoResponse.has("results") && geoResponse.path("results").size() > 0) {
                JsonNode firstResult = geoResponse.path("results").get(0);
                double lat = firstResult.path("latitude").asDouble();
                double lon = firstResult.path("longitude").asDouble();

                String weatherUrl = String.format(
                        "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m&hourly=uv_index&timezone=auto",
                        lat, lon);
                JsonNode response = restTemplate.getForObject(weatherUrl, JsonNode.class);

                if (response != null) {
                    data.setTemp(response.path("current").path("temperature_2m").asDouble());

                    JsonNode uvList = response.path("hourly").path("uv_index");
                    int currentHour = java.time.LocalTime.now().getHour();
                    if (uvList != null && uvList.isArray() && uvList.size() > currentHour) {
                        data.setUvIndex(uvList.get(currentHour).asDouble());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi weather (" + queryCity + "): " + e.getMessage());
            data.setTemp(0.0);
            data.setUvIndex(0.0);
        }
        return data;
    }

    /** Chuẩn hóa tên thành phố tiếng Việt → query Latin cho Open-Meteo. */
    static String normalizeCityForGeocode(String city) {
        if (city == null || city.isBlank()) {
            return "Hanoi";
        }
        String trimmed = city.trim();
        String norm = stripDiacritics(trimmed).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");

        for (String[] row : CITY_ALIASES) {
            if (row.length < 2) continue;
            String query = row[row.length - 1];
            for (int i = 0; i < row.length - 1; i++) {
                String alias = row[i];
                if (norm.equals(alias) || norm.contains(alias) || alias.contains(norm)) {
                    return query;
                }
            }
        }
        return trimmed;
    }

    private static String stripDiacritics(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D');
    }
}
