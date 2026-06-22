package org.example.templatejava6.voucher.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.voucher.model.request.ChiTietDotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.ChiTietDotGiamGiaResponse;
import org.example.templatejava6.voucher.service.ChiTietDotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/chi-tiet-dot-giam-gia")
public class ChiTietDotGiamGiaController {

    @Autowired
    private ChiTietDotGiamGiaService chiTietDotGiamGiaService;

    @GetMapping
    public List<ChiTietDotGiamGiaResponse> hienThiTheoDotGiamGia(@RequestParam("idDotGiamGia") Integer idDotGiamGia) {
        return chiTietDotGiamGiaService.getByDotGiamGia(idDotGiamGia);
    }

    @GetMapping("/{id}")
    public ChiTietDotGiamGiaResponse detail(@PathVariable("id") Integer id) {
        return chiTietDotGiamGiaService.detail(id);
    }

    @PostMapping()
    public void add(@Valid @RequestBody ChiTietDotGiamGiaRequest request) {
        chiTietDotGiamGiaService.add(request);
    }

    @PutMapping("/{id}")
    public void update(@Valid @RequestBody ChiTietDotGiamGiaRequest request, @PathVariable("id") Integer id) {
        chiTietDotGiamGiaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        chiTietDotGiamGiaService.delete(id);
    }
}
