package org.example.templatejava6.auth.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.auth.model.request.DangKyRequest;
import org.example.templatejava6.auth.model.request.DangNhapRequest;
import org.example.templatejava6.auth.model.request.DatLaiMatKhauRequest;
import org.example.templatejava6.auth.model.request.QuenMatKhauRequest;
import org.example.templatejava6.auth.model.response.AuthResponse;
import org.example.templatejava6.auth.model.response.MessageResponse;
import org.example.templatejava6.auth.service.KhachAuthService;
import org.example.templatejava6.auth.service.KhachQuenMatKhauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/khach")
public class KhachAuthController {

    @Autowired
    private KhachAuthService khachAuthService;

    @Autowired
    private KhachQuenMatKhauService khachQuenMatKhauService;

    @PostMapping("/dang-ky")
    public AuthResponse dangKy(@Valid @RequestBody DangKyRequest request) {
        return khachAuthService.dangKy(request);
    }

    @PostMapping("/dang-nhap")
    public AuthResponse dangNhap(@Valid @RequestBody DangNhapRequest request) {
        return khachAuthService.dangNhap(request);
    }

    @PostMapping("/quen-mat-khau")
    public MessageResponse quenMatKhau(@Valid @RequestBody QuenMatKhauRequest request) {
        return khachQuenMatKhauService.quenMatKhau(request);
    }

    @PostMapping("/dat-lai-mat-khau")
    public MessageResponse datLaiMatKhau(@Valid @RequestBody DatLaiMatKhauRequest request) {
        return khachQuenMatKhauService.datLaiMatKhau(request);
    }
}
