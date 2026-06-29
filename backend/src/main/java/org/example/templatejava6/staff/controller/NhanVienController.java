package org.example.templatejava6.staff.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.staff.model.request.NhanVienCreateRequest;
import org.example.templatejava6.staff.model.request.NhanVienDatLaiMatKhauRequest;
import org.example.templatejava6.staff.model.request.NhanVienTrangThaiRequest;
import org.example.templatejava6.staff.model.request.NhanVienUpdateRequest;
import org.example.templatejava6.staff.model.response.DatLaiMatKhauResponse;
import org.example.templatejava6.staff.model.response.NhanVienResponse;
import org.example.templatejava6.staff.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien")
public class NhanVienController {

    @Autowired
    private NhanVienService nhanVienService;

    /** Danh sách đầy đủ — chỉ QUAN_LY (quản lý nhân viên) */
    @GetMapping
    public List<NhanVienResponse> danhSach() {
        return nhanVienService.danhSachQuanLy();
    }

    @GetMapping("/{id:\\d+}")
    public NhanVienResponse chiTiet(@PathVariable Integer id) {
        return nhanVienService.chiTiet(id);
    }

    @PostMapping
    public NhanVienResponse taoMoi(@Valid @RequestBody NhanVienCreateRequest request) {
        return nhanVienService.taoMoi(request);
    }

    @PutMapping("/{id:\\d+}")
    public NhanVienResponse capNhat(
            @PathVariable Integer id,
            @Valid @RequestBody NhanVienUpdateRequest request
    ) {
        return nhanVienService.capNhat(id, request);
    }

    @PatchMapping("/{id:\\d+}/trang-thai")
    public NhanVienResponse doiTrangThai(
            @PathVariable Integer id,
            @Valid @RequestBody NhanVienTrangThaiRequest request
    ) {
        return nhanVienService.doiTrangThai(id, request);
    }

    @PutMapping("/{id:\\d+}/dat-lai-mat-khau")
    public DatLaiMatKhauResponse datLaiMatKhau(
            @PathVariable Integer id,
            @Valid @RequestBody NhanVienDatLaiMatKhauRequest request
    ) {
        return nhanVienService.datLaiMatKhau(id, request);
    }
}
