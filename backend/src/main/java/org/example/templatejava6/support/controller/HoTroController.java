package org.example.templatejava6.support.controller;

import org.example.templatejava6.support.model.request.GuiTinHoTroRequest;
import org.example.templatejava6.support.model.response.PhienHoTroResponse;
import org.example.templatejava6.support.model.response.TinNhanHoTroResponse;
import org.example.templatejava6.support.service.HoTroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ho-tro")
public class HoTroController {

    private final HoTroService hoTroService;

    public HoTroController(HoTroService hoTroService) {
        this.hoTroService = hoTroService;
    }

    /** Khách: tạo hoặc lấy phiên đang mở. */
    @PostMapping("/phien")
    public ResponseEntity<PhienHoTroResponse> taoHoacLayPhien() {
        return ResponseEntity.ok(hoTroService.taoHoacLayPhienKhach());
    }

    /** Nhân viên: danh sách phiên đang mở (shared inbox). */
    @GetMapping("/phien")
    public ResponseEntity<List<PhienHoTroResponse>> danhSachPhienMo() {
        return ResponseEntity.ok(hoTroService.danhSachPhienMo());
    }

    /** Khách gửi tin. */
    @PostMapping("/tin-nhan")
    public ResponseEntity<TinNhanHoTroResponse> guiTinKhach(@RequestBody GuiTinHoTroRequest request) {
        return ResponseEntity.ok(hoTroService.guiTinKhach(request));
    }

    /** Lịch sử tin nhắn (khách hoặc nhân viên). */
    @GetMapping("/phien/{id}/tin-nhan")
    public ResponseEntity<List<TinNhanHoTroResponse>> lichSuTinNhan(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(hoTroService.lichSuTinNhanThongMinh(id));
    }

    /** Nhân viên trả lời. */
    @PostMapping("/phien/{id}/tra-loi")
    public ResponseEntity<TinNhanHoTroResponse> traLoi(
            @PathVariable("id") Integer id,
            @RequestBody GuiTinHoTroRequest request) {
        return ResponseEntity.ok(hoTroService.traLoiNhanVien(id, request));
    }

    /** Nhân viên: đánh dấu đã đọc tin khách trong phiên. */
    @PutMapping("/phien/{id}/da-doc")
    public ResponseEntity<Void> danhDauDaDoc(@PathVariable("id") Integer id) {
        hoTroService.danhDauDaDoc(id);
        return ResponseEntity.noContent().build();
    }
}
