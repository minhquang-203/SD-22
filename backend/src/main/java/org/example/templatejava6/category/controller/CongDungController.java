package org.example.templatejava6.category.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.category.model.request.CongDungRequest;
import org.example.templatejava6.category.model.response.CongDungResponse;
import org.example.templatejava6.category.service.CategoryService;
import org.example.templatejava6.common.model.response.MaTiepTheoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cong-dung")
public class CongDungController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<CongDungResponse> hienThiDanhSach() {
        return categoryService.getAllCongDung();
    }

    @GetMapping("detail")
    public CongDungResponse detail(@RequestParam("id") Integer id) {
        return categoryService.detailCongDung(id);
    }

    @GetMapping("ma-tiep-theo")
    public MaTiepTheoResponse maTiepTheo() {
        return categoryService.previewMaCongDung();
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody CongDungRequest request) {
        categoryService.addCongDung(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody CongDungRequest request, @PathVariable("id") Integer id) {
        categoryService.updateCongDung(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        categoryService.deleteCongDung(id);
    }
}
