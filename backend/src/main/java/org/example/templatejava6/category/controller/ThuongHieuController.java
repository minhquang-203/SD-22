package org.example.templatejava6.category.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.category.model.request.ThuongHieuRequest;
import org.example.templatejava6.category.model.response.ThuongHieuResponse;
import org.example.templatejava6.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/thuong-hieu")
public class ThuongHieuController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<ThuongHieuResponse> hienThiDanhSach() {
        return categoryService.getAllThuongHieu();
    }

    @GetMapping("detail")
    public ThuongHieuResponse detail(@RequestParam("id") Integer id) {
        return categoryService.detailThuongHieu(id);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody ThuongHieuRequest request) {
        categoryService.addThuongHieu(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody ThuongHieuRequest request, @PathVariable("id") Integer id) {
        categoryService.updateThuongHieu(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        categoryService.deleteThuongHieu(id);
    }
}
