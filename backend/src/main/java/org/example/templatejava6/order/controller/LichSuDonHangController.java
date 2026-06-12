package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.LichSuDonHangCapNhatRequest;
import org.example.templatejava6.order.model.request.LichSuDonHangRequest;
import org.example.templatejava6.order.model.response.LichSuDonHangResponse;
import org.example.templatejava6.order.service.LichSuDonHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/lich-su-don-hang")
public class LichSuDonHangController {

    @Autowired
    private LichSuDonHangService lichSuDonHangService;

    @GetMapping
    public List<LichSuDonHangResponse> hienThiDanhSach() {
        return lichSuDonHangService.getAll();
    }

    @GetMapping("hoa-don")
    public List<LichSuDonHangResponse> getByHoaDon(@RequestParam("idHoaDon") Integer idHoaDon) {
        return lichSuDonHangService.getByHoaDon(idHoaDon);
    }

    @GetMapping("detail")
    public LichSuDonHangResponse detail(@RequestParam("id") Integer id) {
        return lichSuDonHangService.detail(id);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody LichSuDonHangRequest request) {
        lichSuDonHangService.add(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody LichSuDonHangCapNhatRequest request, @PathVariable("id") Integer id) {
        lichSuDonHangService.update(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        lichSuDonHangService.delete(id);
    }
}
