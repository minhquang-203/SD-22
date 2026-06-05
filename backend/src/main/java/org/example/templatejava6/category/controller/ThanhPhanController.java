package org.example.templatejava6.category.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.category.model.request.ThanhPhanRequest;
import org.example.templatejava6.category.model.response.ThanhPhanResponse;
import org.example.templatejava6.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/thanh-phan")
public class ThanhPhanController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<ThanhPhanResponse> hienThiDanhSach() {
        return categoryService.getAllThanhPhan();
    }

    @GetMapping("detail")
    public ThanhPhanResponse detail(@RequestParam("id") Integer id) {
        return categoryService.detailThanhPhan(id);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody ThanhPhanRequest request) {
        categoryService.addThanhPhan(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody ThanhPhanRequest request, @PathVariable("id") Integer id) {
        categoryService.updateThanhPhan(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        categoryService.deleteThanhPhan(id);
    }
}
