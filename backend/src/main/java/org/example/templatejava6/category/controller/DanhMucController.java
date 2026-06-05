package org.example.templatejava6.category.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.category.model.request.DanhMucRequest;
import org.example.templatejava6.category.model.response.DanhMucResponse;
import org.example.templatejava6.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/danh-muc")
public class DanhMucController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<DanhMucResponse> hienThiDanhSach() {
        return categoryService.getAllDanhMuc();
    }

    @GetMapping("detail")
    public DanhMucResponse detail(@RequestParam("id") Integer id) {
        return categoryService.detailDanhMuc(id);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody DanhMucRequest request) {
        categoryService.addDanhMuc(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody DanhMucRequest request, @PathVariable("id") Integer id) {
        categoryService.updateDanhMuc(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        categoryService.deleteDanhMuc(id);
    }
}
