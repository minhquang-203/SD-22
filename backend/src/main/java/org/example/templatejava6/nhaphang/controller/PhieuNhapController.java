package org.example.templatejava6.nhaphang.controller;

import org.example.templatejava6.nhaphang.model.request.PhieuNhapRequest;
import org.example.templatejava6.nhaphang.model.response.BienTheNhapHangResponse;
import org.example.templatejava6.nhaphang.model.response.PhieuNhapResponse;
import org.example.templatejava6.nhaphang.service.PhieuNhapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/nhap-hang")
public class PhieuNhapController {

    @Autowired private PhieuNhapService phieuNhapService;

    @GetMapping("tim-bien-the")
    public List<BienTheNhapHangResponse> timBienThe(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return phieuNhapService.timBienThe(keyword, page, size);
    }

    @GetMapping
    public List<PhieuNhapResponse> list(
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) Integer idNcc,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return phieuNhapService.list(trangThai, idNcc, from, to);
    }

    @GetMapping("{id}")
    public PhieuNhapResponse detail(@PathVariable Integer id) {
        return phieuNhapService.detail(id);
    }

    @PostMapping("luu-tam")
    public PhieuNhapResponse luuTam(@RequestBody PhieuNhapRequest request) {
        return phieuNhapService.luuTam(request);
    }

    @PutMapping("{id}")
    public PhieuNhapResponse update(@PathVariable Integer id, @RequestBody PhieuNhapRequest request) {
        return phieuNhapService.updateTam(id, request);
    }

    @PostMapping("{id}/hoan-thanh")
    public PhieuNhapResponse hoanThanh(@PathVariable Integer id) {
        return phieuNhapService.hoanThanh(id);
    }

    @PostMapping("{id}/huy")
    public PhieuNhapResponse huy(@PathVariable Integer id) {
        return phieuNhapService.huy(id);
    }
}
