package org.example.templatejava6.Uv.service;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.templatejava6.Uv.entity.WeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;

@Service
public class WeatherService {
    public WeatherData getWeatherData(String city) {
        WeatherData data = new WeatherData();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Bước 1: Tìm tọa độ từ tên thành phố
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city + "&count=1";
            JsonNode geoResponse = restTemplate.getForObject(geoUrl, JsonNode.class);

            if (geoResponse != null && geoResponse.has("results")) {
                JsonNode firstResult = geoResponse.path("results").get(0);
                double lat = firstResult.path("latitude").asDouble();
                double lon = firstResult.path("longitude").asDouble();

                // Bước 2: Gọi lấy thời tiết và UV bằng tọa độ vừa tìm được
                String weatherUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m&hourly=uv_index&timezone=auto", lat, lon);
                JsonNode response = restTemplate.getForObject(weatherUrl, JsonNode.class);

                if (response != null) {
                    // Nhiệt độ
                    data.setTemp(response.path("current").path("temperature_2m").asDouble());

                    // UV theo giờ hiện tại
                    JsonNode uvList = response.path("hourly").path("uv_index");
                    int currentHour = java.time.LocalTime.now().getHour();
                    data.setUvIndex(uvList.get(currentHour).asDouble());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            data.setTemp(0.0);
            data.setUvIndex(0.0);
        }
        return data;
    }
}
