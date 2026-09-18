package org.example.templatejava6.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.TaoVanDonTraRequest;
import org.example.templatejava6.order.model.request.TaoYeuCauTraHangRequest;
import org.example.templatejava6.order.model.response.StorefrontReturnDetailResponse;
import org.example.templatejava6.order.model.response.YeuCauTraHangResponse;
import org.example.templatejava6.order.service.PublicOrderLookupRateLimiter;
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

/**
 * Trả hàng khách vãng lai: chứng minh sở hữu bằng tracking token (cùng token tra cứu đơn).
 * Không đụng API thành viên {@code /api/online/**}.
 */
@RestController
@RequestMapping("/api/hoa-don/tra-cuu")
public class TraHangKhachVangLaiController {

    private final ReturnRequestService returnRequestService;
    private final PublicOrderLookupRateLimiter publicLookupRateLimiter;

    public TraHangKhachVangLaiController(ReturnRequestService returnRequestService,
                                         PublicOrderLookupRateLimiter publicLookupRateLimiter) {
        this.returnRequestService = returnRequestService;
        this.publicLookupRateLimiter = publicLookupRateLimiter;
    }

    @PostMapping(value = "/{token}/orders/{idHoaDon}/tra-hang", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public YeuCauTraHangResponse taoYeuCau(
            @PathVariable String token,
            @PathVariable Integer idHoaDon,
            @Valid @RequestPart("data") TaoYeuCauTraHangRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        publicLookupRateLimiter.checkOrThrow(clientIp(httpRequest));
        return returnRequestService.taoYeuCauBangToken(token, idHoaDon, request, files);
    }

    @GetMapping("/{token}/tra-hang/ca-lay-hang")
    public List<GhnPickShiftResponse> caLayHang(
            @PathVariable String token,
            HttpServletRequest httpRequest) {
        publicLookupRateLimiter.checkOrThrow(clientIp(httpRequest));
        returnRequestService.assertTrackingToken(token);
        return returnRequestService.danhSachCaLayHang();
    }

    @GetMapping("/{token}/tra-hang/{id}")
    public StorefrontReturnDetailResponse chiTiet(
            @PathVariable String token,
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        publicLookupRateLimiter.checkOrThrow(clientIp(httpRequest));
        return returnRequestService.chiTietBangToken(token, id);
    }

    @PostMapping("/{token}/tra-hang/{id}/tao-van-don")
    public YeuCauTraHangResponse taoVanDonTra(
            @PathVariable String token,
            @PathVariable Integer id,
            @RequestBody(required = false) TaoVanDonTraRequest request,
            HttpServletRequest httpRequest) {
        publicLookupRateLimiter.checkOrThrow(clientIp(httpRequest));
        return returnRequestService.taoVanDonTraBangToken(
                token, id, request != null ? request.getPickShiftId() : null);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
