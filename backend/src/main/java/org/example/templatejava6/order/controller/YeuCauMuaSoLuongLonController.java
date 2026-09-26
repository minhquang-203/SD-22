package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.CapNhatYeuCauMuaSoLuongLonRequest;
import org.example.templatejava6.order.model.request.TaoYeuCauMuaSoLuongLonRequest;
import org.example.templatejava6.order.model.response.YeuCauMuaSoLuongLonResponse;
import org.example.templatejava6.order.service.YeuCauMuaSoLuongLonService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/yeu-cau-mua-so-luong-lon")
public class YeuCauMuaSoLuongLonController {

    private final YeuCauMuaSoLuongLonService service;

    public YeuCauMuaSoLuongLonController(YeuCauMuaSoLuongLonService service) {
        this.service = service;
    }

    /** Công khai — khách chưa đăng nhập cũng gửi được. */
    @PostMapping
    public ResponseEntity<YeuCauMuaSoLuongLonResponse> tao(
            @Valid @RequestBody TaoYeuCauMuaSoLuongLonRequest request) {
        return ResponseEntity.ok(service.tao(request));
    }

    @GetMapping
    public ResponseEntity<Page<YeuCauMuaSoLuongLonResponse>> danhSach(
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.danhSach(trangThai, page, size));
    }

    @GetMapping("/dem-moi")
    public ResponseEntity<Map<String, Long>> demMoi() {
        return ResponseEntity.ok(Map.of("count", service.demMoi()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<YeuCauMuaSoLuongLonResponse> chiTiet(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chiTiet(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<YeuCauMuaSoLuongLonResponse> capNhat(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatYeuCauMuaSoLuongLonRequest request) {
        return ResponseEntity.ok(service.capNhat(id, request));
    }
}
