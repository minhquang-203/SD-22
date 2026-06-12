package org.example.templatejava6.voucher.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.voucher.model.request.DotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.DotGiamGiaResponse;
import org.example.templatejava6.voucher.service.DotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sale")
public class DotGiamGiaController {

    @Autowired
    private DotGiamGiaService dotGiamGiaService;

    @GetMapping
    public List<DotGiamGiaResponse> hienThiDanhSach() {
        return dotGiamGiaService.getAll();
    }

    @GetMapping("/{id}")
    public DotGiamGiaResponse detail(@PathVariable("id") Integer id) {
        return dotGiamGiaService.detail(id);
    }

    @PostMapping
    public void add(@Valid @RequestBody DotGiamGiaRequest request) {
        dotGiamGiaService.add(request);
    }

    @PutMapping("/{id}")
    public void update(@Valid @RequestBody DotGiamGiaRequest request, @PathVariable("id") Integer id) {
        dotGiamGiaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        dotGiamGiaService.delete(id);
    }
}
