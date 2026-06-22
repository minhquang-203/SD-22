package org.example.templatejava6.voucher.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.common.util.PaginationUtil;
import org.example.templatejava6.voucher.model.request.ChiTietDotGiamGiaRequest;
import org.example.templatejava6.voucher.model.request.DotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.ChiTietDotGiamGiaResponse;
import org.example.templatejava6.voucher.model.response.DotGiamGiaResponse;
import org.example.templatejava6.voucher.service.ChiTietDotGiamGiaService;
import org.example.templatejava6.voucher.service.DotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sale")
public class DotGiamGiaController {

    @Autowired
    private DotGiamGiaService dotGiamGiaService;
    @Autowired
    private ChiTietDotGiamGiaService chiTietDotGiamGiaService;

    @GetMapping
    public List<DotGiamGiaResponse> hienThiDanhSach() {
        return dotGiamGiaService.getAll();
    }

    @GetMapping("/{id}")
    public DotGiamGiaResponse detail(@PathVariable("id") Integer id) {
        return dotGiamGiaService.detail(id);
    }

    @GetMapping("search")
    public ResponseEntity<Page<DotGiamGiaResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String timeStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationUtil.create(page, size);
        return ResponseEntity.ok(dotGiamGiaService.search(keyword, timeStatus, pageable));
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

    @GetMapping("/{id}/products")
    public List<ChiTietDotGiamGiaResponse> getProducts(@PathVariable("id") Integer id) {
        return chiTietDotGiamGiaService.getByDotGiamGia(id);
    }

    @PostMapping("/{id}/products")
    public void addProduct(
            @PathVariable("id") Integer id,
            @RequestBody ChiTietDotGiamGiaRequest request) {
        chiTietDotGiamGiaService.addToDotGiamGia(id, request);
    }

    @PutMapping("/{id}/products/{detailId}")
    public void updateProduct(
            @PathVariable("id") Integer id,
            @PathVariable("detailId") Integer detailId,
            @RequestBody ChiTietDotGiamGiaRequest request) {
        chiTietDotGiamGiaService.updateInDotGiamGia(id, detailId, request);
    }

    @DeleteMapping("/{id}/products/{detailId}")
    public void deleteProduct(
            @PathVariable("id") Integer id,
            @PathVariable("detailId") Integer detailId) {
        chiTietDotGiamGiaService.deleteInDotGiamGia(id, detailId);
    }
}
