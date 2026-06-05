package org.example.templatejava6.category.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.category.model.request.MauSacRequest;
import org.example.templatejava6.category.model.response.MauSacResponse;
import org.example.templatejava6.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/mau-sac")
public class MauSacController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<MauSacResponse> hienThiDanhSach() {
        return categoryService.getAllMauSac();
    }

    @GetMapping("detail")
    public MauSacResponse detail(@RequestParam("id") Integer id) {
        return categoryService.detailMauSac(id);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody MauSacRequest request) {
        categoryService.addMauSac(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody MauSacRequest request, @PathVariable("id") Integer id) {
        categoryService.updateMauSac(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        categoryService.deleteMauSac(id);
    }
}
