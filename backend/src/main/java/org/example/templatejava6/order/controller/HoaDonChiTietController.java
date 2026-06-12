package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.HoaDonChiTietRequest;
import org.example.templatejava6.order.model.response.HoaDonChiTietResponse;
import org.example.templatejava6.order.service.HoaDonChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/hoa-don-chi-tiet")
public class HoaDonChiTietController {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @GetMapping("/{id}")
    public HoaDonChiTietResponse detail(@PathVariable("id") Integer id) {
        return hoaDonChiTietService.detail(id);
    }

    @PostMapping
    public void add(@Valid @RequestBody HoaDonChiTietRequest request) {
        hoaDonChiTietService.add(request);
    }

    @PutMapping("/{id}")
    public void update(@Valid @RequestBody HoaDonChiTietRequest request, @PathVariable("id") Integer id) {
        hoaDonChiTietService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestParam("id") Integer id) {
        hoaDonChiTietService.delete(id);
    }
}
