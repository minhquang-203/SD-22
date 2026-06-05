package org.example.templatejava6.product.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.product.model.request.SanPhamRequest;
import org.example.templatejava6.product.model.response.SanPhamDetailResponse;
import org.example.templatejava6.product.model.response.SanPhamResponse;
import org.example.templatejava6.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public List<SanPhamResponse> hienThiDanhSach() {
        return sanPhamService.getAll();
    }

    @GetMapping("padding")
    public List<SanPhamResponse> phanTrang(@RequestParam("pageNo") Integer pageNo,
                                           @RequestParam("pageSize") Integer pageSize) {
        return sanPhamService.phanTrang(pageNo, pageSize).getContent();
    }

    @GetMapping("detail")
    public SanPhamDetailResponse detail(@RequestParam("id") Integer id) {
        return sanPhamService.detail(id);
    }

    @GetMapping("tim")
    public List<SanPhamResponse> timKiem(@RequestParam("keyword") String keyword) {
        return sanPhamService.timKiem(keyword);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody SanPhamRequest request) {
        sanPhamService.add(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody SanPhamRequest request, @PathVariable("id") Integer id) {
        sanPhamService.update(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        sanPhamService.delete(id);
    }
}
