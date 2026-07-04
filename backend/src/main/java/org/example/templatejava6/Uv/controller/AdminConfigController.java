package org.example.templatejava6.Uv.controller;

import org.example.templatejava6.Uv.entity.UvConfig;
import org.example.templatejava6.Uv.repository.ProductRepository;
import org.example.templatejava6.Uv.repository.UvConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // Nhớ mở CORS cho Admin
@RestController
@RequestMapping("/api/v1/admin/config/uv")
public class AdminConfigController {

    @Autowired
    private UvConfigRepository configRepository;
    @Autowired
    private ProductRepository productRepository; // Spring Boot sẽ tự động tạo đối tượng này cho bạn

    // Lấy cấu hình hiện tại lên giao diện
    @GetMapping
    public ResponseEntity<UvConfig> getConfig() {
        // Lấy dòng cấu hình đầu tiên (ID = 1). Nếu chưa có thì tạo mới dòng mặc định.
        UvConfig config = configRepository.findById(1L).orElseGet(() -> {
            UvConfig defaultConfig = new UvConfig();
            return configRepository.save(defaultConfig);
        });
        return ResponseEntity.ok(config);
    }

    // Cập nhật cấu hình khi Admin ấn "Lưu"
    @PutMapping
    public ResponseEntity<UvConfig> updateConfig(@RequestBody UvConfig updateData) {
        UvConfig config = configRepository.findById(1L).orElse(new UvConfig());

        // Cập nhật các giá trị mới
        config.setUvHighThreshold(updateData.getUvHighThreshold());
        config.setUvExtremeThreshold(updateData.getUvExtremeThreshold());
        config.setBonusPointsHigh(updateData.getBonusPointsHigh());
        config.setBonusPointsExtreme(updateData.getBonusPointsExtreme());
        config.setEnableRealtimeAlert(updateData.isEnableRealtimeAlert());

        // Lưu lại vào DB
        UvConfig savedConfig = configRepository.save(config);
        return ResponseEntity.ok(savedConfig);
    }
}
