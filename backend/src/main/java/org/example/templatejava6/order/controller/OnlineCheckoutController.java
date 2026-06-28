package org.example.templatejava6.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.HuyDonOnlineRequest;
import org.example.templatejava6.order.model.request.OnlineCheckoutRequest;
import org.example.templatejava6.order.model.response.HoaDonDetailResponse;
import org.example.templatejava6.order.model.response.HoaDonResponse;
import org.example.templatejava6.order.model.response.OnlineCheckoutResponse;
import org.example.templatejava6.order.service.OnlineCheckoutService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/online")
public class OnlineCheckoutController {

    private final OnlineCheckoutService onlineCheckoutService;

    public OnlineCheckoutController(OnlineCheckoutService onlineCheckoutService) {
        this.onlineCheckoutService = onlineCheckoutService;
    }

    @PostMapping("/checkout")
    public OnlineCheckoutResponse checkout(
            @Valid @RequestBody OnlineCheckoutRequest request,
            HttpServletRequest servletRequest) {
        return onlineCheckoutService.checkout(request, getClientIp(servletRequest));
    }

    @GetMapping("/orders")
    public List<HoaDonResponse> danhSachDonHang(@RequestParam Integer idKhachHang) {
        return onlineCheckoutService.danhSachDonHang(idKhachHang);
    }

    @GetMapping("/orders/{idHoaDon}")
    public HoaDonDetailResponse chiTietDonHang(
            @PathVariable Integer idHoaDon,
            @RequestParam Integer idKhachHang) {
        return onlineCheckoutService.chiTietDonHang(idKhachHang, idHoaDon);
    }

    @PatchMapping("/orders/{idHoaDon}/cancel")
    public HoaDonDetailResponse huyDonHang(
            @PathVariable Integer idHoaDon,
            @RequestParam Integer idKhachHang,
            @RequestBody(required = false) HuyDonOnlineRequest request) {
        return onlineCheckoutService.huyDonHang(idKhachHang, idHoaDon, request);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
