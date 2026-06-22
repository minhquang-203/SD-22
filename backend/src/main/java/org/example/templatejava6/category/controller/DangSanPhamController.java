package org.example.templatejava6.category.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.category.model.request.DangSanPhamRequest;
import org.example.templatejava6.category.model.response.DangSanPhamResponse;
import org.example.templatejava6.category.service.CategoryService;
import org.example.templatejava6.common.model.response.MaTiepTheoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/dang-san-pham")
public class DangSanPhamController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<DangSanPhamResponse> hienThiDanhSach() {
        return categoryService.getAllDangSanPham();
    }

    @GetMapping("detail")
    public DangSanPhamResponse detail(@RequestParam("id") Integer id) {
        return categoryService.detailDangSanPham(id);
    }

    @GetMapping("ma-tiep-theo")
    public MaTiepTheoResponse maTiepTheo() {
        return categoryService.previewMaDangSanPham();
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody DangSanPhamRequest request) {
        categoryService.addDangSanPham(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody DangSanPhamRequest request, @PathVariable("id") Integer id) {
        categoryService.updateDangSanPham(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        categoryService.deleteDangSanPham(id);
    }
}
