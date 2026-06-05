package org.example.templatejava6.product.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.product.model.request.ChiTietSanPhamRequest;
import org.example.templatejava6.product.model.response.ChiTietSanPhamResponse;
import org.example.templatejava6.product.service.ChiTietSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/chi-tiet-san-pham")
public class ChiTietSanPhamController {

    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    @GetMapping("detail")
    public ChiTietSanPhamResponse detail(@RequestParam("id") Integer id) {
        return chiTietSanPhamService.detail(id);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody ChiTietSanPhamRequest request) {
        chiTietSanPhamService.add(request);
    }

    @PutMapping("update/{id}")
    public void update(@Valid @RequestBody ChiTietSanPhamRequest request, @PathVariable("id") Integer id) {
        chiTietSanPhamService.update(id, request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        chiTietSanPhamService.delete(id);
    }
}
