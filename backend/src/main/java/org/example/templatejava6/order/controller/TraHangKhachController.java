package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.TaoYeuCauTraHangRequest;
import org.example.templatejava6.order.model.response.YeuCauTraHangResponse;
import org.example.templatejava6.order.service.ReturnRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoint tra hang cho khach hang (storefront). Nam duoi /api/online (role KHACH_HANG). */
@RestController
@RequestMapping("/api/online")
public class TraHangKhachController {

    private final ReturnRequestService returnRequestService;

    public TraHangKhachController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @PostMapping("/orders/{idHoaDon}/tra-hang")
    public YeuCauTraHangResponse taoYeuCau(
            @PathVariable Integer idHoaDon,
            @RequestParam Integer idKhachHang,
            @Valid @RequestBody TaoYeuCauTraHangRequest request) {
        return returnRequestService.taoYeuCau(idKhachHang, idHoaDon, request);
    }

    @GetMapping("/tra-hang")
    public List<YeuCauTraHangResponse> danhSachCuaToi(@RequestParam Integer idKhachHang) {
        return returnRequestService.danhSachCuaToi(idKhachHang);
    }

    @PostMapping("/tra-hang/{id}/tao-van-don")
    public YeuCauTraHangResponse taoVanDonTra(
            @PathVariable Integer id,
            @RequestParam Integer idKhachHang) {
        return returnRequestService.taoVanDonTra(idKhachHang, id);
    }
}
