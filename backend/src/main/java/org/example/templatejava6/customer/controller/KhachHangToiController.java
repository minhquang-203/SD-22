package org.example.templatejava6.customer.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.customer.model.request.CapNhatKhachHangToiRequest;
import org.example.templatejava6.customer.model.request.DoiMatKhauToiRequest;
import org.example.templatejava6.customer.model.response.KhachHangToiResponse;
import org.example.templatejava6.customer.service.KhachHangToiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/khach-hang/toi")
public class KhachHangToiController {

    @Autowired
    private KhachHangToiService khachHangToiService;

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
}
