package org.example.templatejava6.order.controller;

import org.example.templatejava6.common.enums.TrangThaiHoanTien;
import org.example.templatejava6.order.model.request.HoaDonTuChoiRequest;
import org.example.templatejava6.order.model.request.HoanTatHoanTienRequest;
import org.example.templatejava6.order.model.response.HoanTienResponse;
import org.example.templatejava6.order.service.RefundService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Endpoint quan ly hoan tien cho admin. */
@RestController
@RequestMapping("/api/hoan-tien")
public class HoanTienController {

    private final RefundService refundService;

    public HoanTienController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    public List<HoanTienResponse> danhSach(
            @RequestParam(required = false) TrangThaiHoanTien trangThai) {
        return refundService.danhSach(trangThai);
    }

    /** VNPAY / goi JSON thuong: khong can anh. */
    @PostMapping(value = "/{id}/hoan-tat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HoanTienResponse hoanTatJson(
            @PathVariable Integer id,
            @RequestBody(required = false) HoanTatHoanTienRequest request) {
        return refundService.hoanTat(id, request, null);
    }

    /** COD / chuyen khoan: ma GD + ghi chu + anh chung tu. */
    @PostMapping(value = "/{id}/hoan-tat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HoanTienResponse hoanTatMultipart(
            @PathVariable Integer id,
            @RequestPart(value = "data", required = false) HoanTatHoanTienRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return refundService.hoanTat(id, request, files);
    }

    @PostMapping("/{id}/tu-choi")
    public HoanTienResponse tuChoi(
            @PathVariable Integer id,
            @RequestBody(required = false) HoaDonTuChoiRequest request) {
        return refundService.tuChoi(
                id,
                request != null ? request.getGhiChu() : null,
                request != null ? request.getIdNhanVien() : null);
    }
}
