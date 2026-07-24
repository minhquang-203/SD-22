package org.example.templatejava6.Uv.controller;

import org.example.templatejava6.Uv.entity.SanPhamUV;
import org.example.templatejava6.Uv.entity.UvConfig;
import org.example.templatejava6.Uv.entity.WeatherData;
import org.example.templatejava6.Uv.repository.ProductRepository;
import org.example.templatejava6.Uv.repository.UvConfigRepository;
import org.example.templatejava6.Uv.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
@CrossOrigin("*")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private UvConfigRepository uvConfigRepository;

    @Autowired
    private ProductRepository productRepository;

    // API 1: Lấy thời tiết + cảnh báo UV (giữ nguyên, không đổi)
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(@RequestParam String city) {
        WeatherData data = weatherService.getWeatherData(city);

        List<UvConfig> configs = uvConfigRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        double threshold = (!configs.isEmpty()) ? configs.get(0).getUvHighThreshold() : 6.0;

        Map<String, Object> response = new HashMap<>();
        response.put("weather", data);
        response.put("isHighAlert", data.getUvIndex() >= threshold);

        return ResponseEntity.ok(response);
    }

    // API 2: Gợi ý sản phẩm - lọc đúng theo mức SPF phù hợp với UV, không fallback
    // UV 0-2   -> SPF 15+
    // UV 3-5   -> SPF 30+
    // UV 6-7   -> SPF 50, PA+++
    // UV 8+    -> SPF 50+, PA++++
    @GetMapping("/suggest")
    public ResponseEntity<?> suggestProducts(@RequestParam double uvIndex) {
        String spfKeyword;

        if (uvIndex <= 2) {
            spfKeyword = "15";
        } else if (uvIndex <= 5) {
            spfKeyword = "30";
        } else if (uvIndex <= 7) {
            spfKeyword = "50";
        } else {
            spfKeyword = "50+";
        }

        List<SanPhamUV> sanPhamGoiY = productRepository.findByChiSoSpfContainingIgnoreCase(spfKeyword);

        return ResponseEntity.ok(sanPhamGoiY);
    }
}