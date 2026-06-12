package org.example.templatejava6.order.controller;

import org.example.templatejava6.order.model.request.GiuDonChoRequest;
import org.example.templatejava6.order.model.request.TaoDonTaiQuayRequest;
import org.example.templatejava6.order.model.response.BienTheBanResponse;
import org.example.templatejava6.order.model.response.DonChoDetailResponse;
import org.example.templatejava6.order.model.response.DonChoListItemResponse;
import org.example.templatejava6.order.model.response.GiuDonChoResponse;
import org.example.templatejava6.order.model.response.BanHangHoaDonResponse;
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

    @PostMapping("tai-quay")
    public BanHangHoaDonResponse taoDonTaiQuay(@RequestBody TaoDonTaiQuayRequest request) {
        return banHangService.taoDonTaiQuay(request);
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
}
