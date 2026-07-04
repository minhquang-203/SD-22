package org.example.templatejava6.Uv.controller;

import org.example.templatejava6.Uv.entity.UvConfig;
import org.example.templatejava6.Uv.entity.WeatherData;
import org.example.templatejava6.Uv.repository.UvConfigRepository;
import org.example.templatejava6.Uv.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
@CrossOrigin("*") // Cho phép Frontend gọi API
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UvConfigRepository uvConfigRepository;

    // API 1: Lấy thời tiết + Trạng thái cảnh báo UV (so với cấu hình trong DB)
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(@RequestParam String city) {
        // 1. Lấy dữ liệu thời tiết
        WeatherData data = weatherService.getWeatherData(city);

        // 2. Lấy ngưỡng cảnh báo từ DB (bản ghi mới nhất)
        List<UvConfig> configs = uvConfigRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        double threshold = (!configs.isEmpty()) ? configs.get(0).getUvHighThreshold() : 6.0;

        // 3. Đóng gói kết quả: dữ liệu thời tiết + flag cảnh báo
        Map<String, Object> response = new HashMap<>();
        response.put("weather", data);
        response.put("isHighAlert", data.getUvIndex() >= threshold);

        return ResponseEntity.ok(response);
    }

    // API 2: Gợi ý sản phẩm theo luật UV
    @GetMapping("/suggest")
    public ResponseEntity<List<Map<String, Object>>> getSuggestedProducts(@RequestParam double uvIndex) {
        String keyword;

        // BỘ LUẬT GỢI Ý
        if (uvIndex >= 8.0) {
            keyword = "Chống nắng";
        } else if (uvIndex >= 5.0) {
            keyword = "Nâng tông";
        } else {
            keyword = "Dịu";
        }

        // Truy vấn sản phẩm (dùng LIMIT cho MySQL)
        String sql = "SELECT ten, chi_so_spf, ma_san_pham FROM san_pham WHERE ten LIKE ? LIMIT 4";
        List<Map<String, Object>> products = jdbcTemplate.queryForList(sql, "%" + keyword + "%");

        return ResponseEntity.ok(products);
    }
}