package org.example.templatejava6.cart.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.cart.model.request.CapNhatSoLuongGioHangRequest;
import org.example.templatejava6.cart.model.request.ThemGioHangRequest;
import org.example.templatejava6.cart.model.response.GioHangResponse;
import org.example.templatejava6.cart.service.GioHangService;
import org.example.templatejava6.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gio-hang")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    @GetMapping
    public GioHangResponse getByKhachHang() {
        return gioHangService.getByKhachHang(currentCustomerId());
    }

    @PostMapping("add")
    public GioHangResponse add(@Valid @RequestBody ThemGioHangRequest request) {
        request.setIdKhachHang(currentCustomerId());
        return gioHangService.add(request);
    }

    @PutMapping("update/{idChiTietGioHang}")
    public GioHangResponse updateSoLuong(@PathVariable("idChiTietGioHang") Integer idChiTietGioHang,
                                         @Valid @RequestBody CapNhatSoLuongGioHangRequest request) {
        return gioHangService.updateSoLuong(currentCustomerId(), idChiTietGioHang, request);
    }

    @DeleteMapping("delete/{idChiTietGioHang}")
    public GioHangResponse deleteItem(@PathVariable("idChiTietGioHang") Integer idChiTietGioHang) {
        return gioHangService.deleteItem(currentCustomerId(), idChiTietGioHang);
    }

    @DeleteMapping("clear")
    public GioHangResponse clear() {
        return gioHangService.clear(currentCustomerId());
    }

    private Integer currentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
    }
}
