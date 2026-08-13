package org.example.templatejava6.notification.controller;

import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.notification.model.response.ThongBaoTongQuanResponse;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Chuông thông báo phía khách hàng (storefront).
 * ID khách lấy từ JWT subject — chỉ trả về thông báo của chính khách đang đăng nhập.
 */
@RestController
@RequestMapping("/api/khach-hang/toi/thong-bao")
public class ThongBaoKhachController {

    private final ThongBaoService thongBaoService;

    public ThongBaoKhachController(ThongBaoService thongBaoService) {
        this.thongBaoService = thongBaoService;
    }

    @GetMapping
    public ThongBaoTongQuanResponse danhSach() {
        return thongBaoService.tongQuanKhach(SecurityUtils.currentKhachHangId());
    }

    @GetMapping("/chua-doc")
    public Map<String, Long> demChuaDoc() {
        return Map.of("soChuaDoc", thongBaoService.demChuaDocKhach(SecurityUtils.currentKhachHangId()));
    }

    @PostMapping("/doc-tat-ca")
    public ResponseEntity<Void> docTatCa() {
        thongBaoService.danhDauDaDocTatCaKhach(SecurityUtils.currentKhachHangId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/doc")
    public ResponseEntity<Void> doc(@PathVariable Integer id) {
        thongBaoService.danhDauDaDocKhach(SecurityUtils.currentKhachHangId(), id);
        return ResponseEntity.noContent().build();
    }
}
