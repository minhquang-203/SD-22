package org.example.templatejava6.customer.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.customer.model.request.CapNhatKhachHangToiRequest;
import org.example.templatejava6.customer.model.request.DiaChiKhachHangRequest;
import org.example.templatejava6.customer.model.request.DoiMatKhauToiRequest;
import org.example.templatejava6.customer.model.response.DiaChiResponse;
import org.example.templatejava6.customer.model.response.KhachHangToiResponse;
import org.example.templatejava6.customer.service.DiaChiKhachHangToiService;
import org.example.templatejava6.customer.service.KhachHangToiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/khach-hang/toi")
public class KhachHangToiController {

    @Autowired
    private KhachHangToiService khachHangToiService;

    @Autowired
    private DiaChiKhachHangToiService diaChiKhachHangToiService;

    @GetMapping
    public KhachHangToiResponse layThongTin() {
        return khachHangToiService.layThongTinToi();
    }

    @PutMapping
    public KhachHangToiResponse capNhat(@Valid @RequestBody CapNhatKhachHangToiRequest request) {
        return khachHangToiService.capNhatThongTin(request);
    }

    @PutMapping("doi-mat-khau")
    public void doiMatKhau(@Valid @RequestBody DoiMatKhauToiRequest request) {
        khachHangToiService.doiMatKhau(request);
    }

    @GetMapping("dia-chi")
    public java.util.List<DiaChiResponse> danhSachDiaChi() {
        return diaChiKhachHangToiService.danhSach();
    }

    @PostMapping("dia-chi")
    public DiaChiResponse taoDiaChi(@Valid @RequestBody DiaChiKhachHangRequest request) {
        return diaChiKhachHangToiService.tao(request);
    }

    @PutMapping("dia-chi/{id}")
    public DiaChiResponse capNhatDiaChi(@PathVariable Integer id, @Valid @RequestBody DiaChiKhachHangRequest request) {
        return diaChiKhachHangToiService.capNhat(id, request);
    }
}
