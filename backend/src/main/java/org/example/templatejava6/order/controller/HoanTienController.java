package org.example.templatejava6.order.controller;

import org.example.templatejava6.common.enums.TrangThaiHoanTien;
import org.example.templatejava6.order.model.request.HoaDonTuChoiRequest;
import org.example.templatejava6.order.model.request.HoanTatHoanTienRequest;
import org.example.templatejava6.order.model.response.HoanTienResponse;
import org.example.templatejava6.order.service.RefundService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/{id}/hoan-tat")
    public HoanTienResponse hoanTat(
            @PathVariable Integer id,
            @RequestBody(required = false) HoanTatHoanTienRequest request) {
        return refundService.hoanTat(id, request);
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
