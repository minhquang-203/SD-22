package org.example.templatejava6.voucher.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.common.util.PaginationUtil;
import org.example.templatejava6.customer.model.response.KhachHangResponse;
import org.example.templatejava6.voucher.model.request.GanVoucherRequest;
import org.example.templatejava6.voucher.model.request.GanVoucherTheoNhomRequest;
import org.example.templatejava6.voucher.model.request.PhieuGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaResponse;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaStatsResponse;
import org.example.templatejava6.voucher.service.PhieuGiamGiaService;
import org.example.templatejava6.voucher.service.VoucherKhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/vouchers")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @Autowired
    private VoucherKhachHangService voucherKhachHangService;

    @GetMapping
    public ResponseEntity<Page<PhieuGiamGiaResponse>> getAllPhieuGiamGia
            (@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationUtil.create(page, size);
        return ResponseEntity.ok(phieuGiamGiaService.getAll(pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<PhieuGiamGiaStatsResponse> getStats() {
        return ResponseEntity.ok(phieuGiamGiaService.getStats());
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

    @PutMapping("/{id}/stop")
    public void stop(@PathVariable Integer id) {
        phieuGiamGiaService.stop(id);
    }

    @PutMapping("/{id}/activate")
    public void activate(@PathVariable Integer id) {
        phieuGiamGiaService.activate(id);
    }

    // Các cột được phép sắp xếp (whitelist chống PropertyReferenceException / SQL injection)
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("ngayBatDau", "ngayKetThuc", "giaTri", "soLuong", "ten", "ma");

    @GetMapping("search")
    public ResponseEntity<Page<PhieuGiamGiaResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String timeStatus,
            @RequestParam(required = false) String loai,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(phieuGiamGiaService.search(keyword, timeStatus, loai, pageable));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (sortBy == null || sortBy.isBlank() || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return PaginationUtil.create(page, size);
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PaginationUtil.create(page, size, Sort.by(dir, sortBy));
    }

    /* ===================== GÁN VOUCHER CÁ NHÂN ===================== */

    // Danh sách khách hàng đã được gán voucher
    @GetMapping("/{id}/khach-hang")
    public ResponseEntity<List<KhachHangResponse>> danhSachKhachDaGan(@PathVariable Integer id) {
        return ResponseEntity.ok(voucherKhachHangService.danhSachKhachDaGan(id));
    }

    // Gán voucher cho các khách hàng cụ thể
    @PostMapping("/{id}/khach-hang")
    public ResponseEntity<Map<String, Integer>> ganChoKhachHang(
            @PathVariable Integer id,
            @Valid @RequestBody GanVoucherRequest request) {
        int added = voucherKhachHangService.ganChoKhachHang(id, request.getIdKhachHangs());
        return ResponseEntity.ok(Map.of("soKhachGanMoi", added));
    }

    // Gán voucher theo bộ lọc nhóm
    @PostMapping("/{id}/khach-hang/theo-nhom")
    public ResponseEntity<Map<String, Integer>> ganTheoNhom(
            @PathVariable Integer id,
            @RequestBody GanVoucherTheoNhomRequest request) {
        int added = voucherKhachHangService.ganTheoNhom(id, request);
        return ResponseEntity.ok(Map.of("soKhachGanMoi", added));
    }

    // Xem trước khách hàng khớp bộ lọc nhóm (không lưu)
    @PostMapping("/khach-hang/theo-nhom/preview")
    public ResponseEntity<List<KhachHangResponse>> previewNhom(
            @RequestBody GanVoucherTheoNhomRequest request) {
        return ResponseEntity.ok(voucherKhachHangService.previewNhom(request));
    }

    // Bỏ gán voucher khỏi một khách hàng
    @DeleteMapping("/{id}/khach-hang/{idKhachHang}")
    public void boGan(@PathVariable Integer id, @PathVariable Integer idKhachHang) {
        voucherKhachHangService.boGan(id, idKhachHang);
    }

}