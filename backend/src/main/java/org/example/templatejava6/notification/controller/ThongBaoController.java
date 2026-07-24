package org.example.templatejava6.notification.controller;

import org.example.templatejava6.notification.model.response.ThongBaoTongQuanResponse;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/thong-bao")
public class ThongBaoController {

    private final ThongBaoService thongBaoService;

    public ThongBaoController(ThongBaoService thongBaoService) {
        this.thongBaoService = thongBaoService;
    }

    /** Danh sách thông báo mới nhất + số lượng chưa đọc (cho chuông admin). */
    @GetMapping
    public ThongBaoTongQuanResponse danhSach() {
        return thongBaoService.tongQuan();
    }

    @GetMapping("/chua-doc")
    public Map<String, Long> demChuaDoc() {
        return Map.of("soChuaDoc", thongBaoService.demChuaDoc());
    }

    /** Đánh dấu đã đọc tất cả — dùng khi mở chuông thông báo. */
    @PostMapping("/doc-tat-ca")
    public ResponseEntity<Void> docTatCa() {
        thongBaoService.danhDauDaDocTatCa();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/doc")
    public ResponseEntity<Void> doc(@PathVariable Integer id) {
        thongBaoService.danhDauDaDoc(id);
        return ResponseEntity.noContent().build();
    }
}
