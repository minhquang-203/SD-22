package org.example.templatejava6.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.templatejava6.order.model.request.GiuDonChoRequest;
import org.example.templatejava6.order.model.request.PosTinhGiaRequest;
import org.example.templatejava6.order.model.request.TaoDonTaiQuayRequest;
import org.example.templatejava6.order.model.response.BienTheBanResponse;
import org.example.templatejava6.order.model.response.DonChoDetailResponse;
import org.example.templatejava6.order.model.response.DonChoListItemResponse;
import org.example.templatejava6.order.model.response.GiuDonChoResponse;
import org.example.templatejava6.order.model.response.PosTinhGiaResponse;
import org.example.templatejava6.order.model.response.BanHangHoaDonResponse;
import org.example.templatejava6.order.model.response.PosThanhToanStatusResponse;
import org.example.templatejava6.order.service.BanHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/ban-hang")
public class BanHangController {

    @Autowired
    private BanHangService banHangService;

    @GetMapping("san-pham")
    public List<BienTheBanResponse> danhSachSanPham(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "0") Integer page) {
        return banHangService.danhSachSanPhamBan(keyword, page);
    }

    @GetMapping("san-pham/tim")
    public List<BienTheBanResponse> timSanPham(@RequestParam(value = "keyword", defaultValue = "") String keyword) {
        return banHangService.timSanPhamBan(keyword);
    }

    @PostMapping("tinh-gia")
    public PosTinhGiaResponse tinhGia(@RequestBody PosTinhGiaRequest request) {
        return banHangService.tinhGiaTaiQuay(request);
    }

    @PostMapping("tai-quay")
    public BanHangHoaDonResponse taoDonTaiQuay(
            @RequestBody TaoDonTaiQuayRequest request,
            HttpServletRequest servletRequest) {
        return banHangService.taoDonTaiQuay(request, getClientIp(servletRequest));
    }

    @GetMapping("tai-quay/{id}/thanh-toan")
    public PosThanhToanStatusResponse kiemTraThanhToan(@PathVariable("id") Integer id) {
        return banHangService.kiemTraThanhToanTaiQuay(id);
    }

    @PostMapping("tai-quay/{id}/huy-thanh-toan")
    public void huyThanhToan(@PathVariable("id") Integer id) {
        banHangService.huyThanhToanVnpayTaiQuay(id);
    }

    @PostMapping("cho")
    public GiuDonChoResponse giuDonCho(@RequestBody GiuDonChoRequest request) {
        return banHangService.giuDonCho(request);
    }

    @GetMapping("cho")
    public List<DonChoListItemResponse> danhSachDonCho() {
        return banHangService.danhSachDonCho();
    }

    @GetMapping("cho/{id}")
    public DonChoDetailResponse chiTietDonCho(@PathVariable("id") Integer id) {
        return banHangService.chiTietDonCho(id);
    }

    @DeleteMapping("cho/{id}")
    public void huyDonCho(@PathVariable("id") Integer id) {
        banHangService.huyDonCho(id);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
