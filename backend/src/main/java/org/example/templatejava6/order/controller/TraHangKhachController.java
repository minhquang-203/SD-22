package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.order.model.request.TaoVanDonTraRequest;
import org.example.templatejava6.order.model.request.TaoYeuCauTraHangRequest;
import org.example.templatejava6.order.model.response.StorefrontReturnDetailResponse;
import org.example.templatejava6.order.model.response.YeuCauTraHangResponse;
import org.example.templatejava6.order.service.ReturnRequestService;
import org.example.templatejava6.shipping.model.response.GhnPickShiftResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Endpoint tra hang cho khach hang (storefront). Nam duoi /api/online (role KHACH_HANG). */
@RestController
@RequestMapping("/api/online")
public class TraHangKhachController {

    private final ReturnRequestService returnRequestService;

    public TraHangKhachController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @PostMapping(value = "/orders/{idHoaDon}/tra-hang", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public YeuCauTraHangResponse taoYeuCau(
            @PathVariable Integer idHoaDon,
            @Valid @RequestPart("data") TaoYeuCauTraHangRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        // idKhachHang chỉ lấy từ JWT — không nhận query để tránh khách A thao tác đơn của B.
        return returnRequestService.taoYeuCau(SecurityUtils.currentKhachHangId(), idHoaDon, request, files);
    }

    @GetMapping("/tra-hang")
    public List<YeuCauTraHangResponse> danhSachCuaToi() {
        return returnRequestService.danhSachCuaToi(SecurityUtils.currentKhachHangId());
    }

    @GetMapping("/tra-hang/{id}")
    public StorefrontReturnDetailResponse chiTietCuaToi(@PathVariable Integer id) {
        return returnRequestService.chiTietCuaToi(SecurityUtils.currentKhachHangId(), id);
    }

    /** Ca lay hang GHN de khach chon thoi diem shipper den lay hang tra. */
    @GetMapping("/tra-hang/ca-lay-hang")
    public List<GhnPickShiftResponse> caLayHang() {
        return returnRequestService.danhSachCaLayHang();
    }

    @PostMapping("/tra-hang/{id}/tao-van-don")
    public YeuCauTraHangResponse taoVanDonTra(
            @PathVariable Integer id,
            @RequestBody(required = false) TaoVanDonTraRequest request) {
        return returnRequestService.taoVanDonTra(
                SecurityUtils.currentKhachHangId(),
                id,
                request != null ? request.getPickShiftId() : null);
    }
}
