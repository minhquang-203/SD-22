package org.example.templatejava6.order.controller;

import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.order.model.request.DuyetTraHangRequest;
import org.example.templatejava6.order.model.request.HoaDonTuChoiRequest;
import org.example.templatejava6.order.model.request.NhanHangTraRequest;
import org.example.templatejava6.order.model.response.LoHangDonHangResponse;
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

/** Endpoint quan ly yeu cau tra hang cho admin. */
@RestController
@RequestMapping("/api/tra-hang")
public class TraHangController {

    private final ReturnRequestService returnRequestService;

    public TraHangController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @GetMapping
    public List<YeuCauTraHangResponse> danhSach(
            @RequestParam(required = false) TrangThaiTraHang trangThai) {
        return returnRequestService.danhSach(trangThai);
    }

    @PostMapping("/{id}/duyet")
    public YeuCauTraHangResponse duyet(
            @PathVariable Integer id,
            @RequestBody(required = false) DuyetTraHangRequest request) {
        return returnRequestService.duyet(
                id,
                request != null ? request.getIdNhanVien() : null,
                request != null ? request.getGhiChu() : null);
    }

    @PostMapping("/{id}/tu-choi")
    public YeuCauTraHangResponse tuChoi(
            @PathVariable Integer id,
            @RequestBody(required = false) HoaDonTuChoiRequest request) {
        return returnRequestService.tuChoi(
                id,
                request != null ? request.getGhiChu() : null,
                request != null ? request.getIdNhanVien() : null);
    }

    @PostMapping("/{id}/da-nhan-hang")
    public YeuCauTraHangResponse daNhanHang(
            @PathVariable Integer id,
            @RequestBody(required = false) NhanHangTraRequest request) {
        return returnRequestService.xacNhanNhanHang(
                id,
                request != null ? request.getIdNhanVien() : null,
                request != null ? request.getChiTietLo() : null);
    }

    /** Danh sách lô đơn đã lấy — để nhân viên chọn khi nhận hàng trả. */
    @GetMapping("/{id}/lo-hang")
    public List<LoHangDonHangResponse> danhSachLo(@PathVariable Integer id) {
        return returnRequestService.danhSachLoCuaYeuCau(id);
    }

    /** Dong bo trang thai van don hoan tu GHN; khong tu hoan kho — can nhan vien phan loai TOT/LOI. */
    @PostMapping("/{id}/dong-bo-ghn")
    public YeuCauTraHangResponse dongBoGhn(
            @PathVariable Integer id,
            @RequestBody(required = false) DuyetTraHangRequest request) {
        return returnRequestService.dongBoVanDonTra(
                id,
                request != null ? request.getIdNhanVien() : null);
    }
}
