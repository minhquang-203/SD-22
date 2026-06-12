package org.example.templatejava6.voucher.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.common.util.PaginationUtil;
import org.example.templatejava6.voucher.model.request.PhieuGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaResponse;
import org.example.templatejava6.voucher.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/vouchers")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping
    public ResponseEntity<Page<PhieuGiamGiaResponse>> getAllPhieuGiamGia
            (@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationUtil.create(page, size);
        return ResponseEntity.ok(phieuGiamGiaService.getAll(pageable));
    }


    // 2. GET BY ID
    @GetMapping("/{id}")
    public PhieuGiamGiaResponse getById(@PathVariable Integer id) {
        return phieuGiamGiaService.detail(id);
    }

    // 3. CREATE
    @PostMapping
    public void create(@Valid @RequestBody PhieuGiamGiaRequest request) {
        phieuGiamGiaService.add(request);
    }

    // 4. UPDATE
    @PutMapping("/{id}")
    public void update(@PathVariable Integer id,
                       @Valid @RequestBody PhieuGiamGiaRequest request) {
        phieuGiamGiaService.update(id, request);
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        phieuGiamGiaService.delete(id);
    }

    @GetMapping("search")
    public ResponseEntity<Page<PhieuGiamGiaResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String timeStatus,
            @RequestParam(required = false) String loai,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PaginationUtil.create(page, size);
        return ResponseEntity.ok(phieuGiamGiaService.search(keyword, timeStatus, loai, pageable));
    }

}