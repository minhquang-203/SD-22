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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    // Các cột được phép sắp xếp (whitelist chống PropertyReferenceException / SQL injection)
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("ngayBatDau", "ngayKetThuc", "phanTramGiam", "ten", "ma");

    @GetMapping("search")
    public ResponseEntity<Page<DotGiamGiaResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String timeStatus,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(dotGiamGiaService.search(keyword, timeStatus, pageable));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (sortBy == null || sortBy.isBlank() || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return PaginationUtil.create(page, size);
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PaginationUtil.create(page, size, Sort.by(dir, sortBy));
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

    @PutMapping("/{id}/stop")
    public void stop(@PathVariable("id") Integer id) {
        dotGiamGiaService.stop(id);
    }

    @PutMapping("/{id}/activate")
    public void activate(@PathVariable("id") Integer id) {
        dotGiamGiaService.activate(id);
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
