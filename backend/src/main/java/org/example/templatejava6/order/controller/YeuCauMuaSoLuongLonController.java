package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.TaoYeuCauMuaSoLuongLonRequest;
import org.example.templatejava6.order.service.YeuCauMuaSoLuongLonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API công khai: khách gửi yêu cầu mua số lượng lớn → hệ thống gửi email cửa hàng.
 * Không có API danh sách / cập nhật trạng thái (không có màn admin).
 */
@RestController
@RequestMapping("/api/yeu-cau-mua-so-luong-lon")
public class YeuCauMuaSoLuongLonController {

    private final YeuCauMuaSoLuongLonService service;

    public YeuCauMuaSoLuongLonController(YeuCauMuaSoLuongLonService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> gui(
            @Valid @RequestBody TaoYeuCauMuaSoLuongLonRequest request) {
        return ResponseEntity.ok(service.guiYeuCau(request));
    }
}
