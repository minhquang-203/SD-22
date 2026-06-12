package org.example.templatejava6.staff.controller;

import org.example.templatejava6.staff.model.response.NhanVienResponse;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/nhan-vien")
public class NhanVienController {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public List<NhanVienResponse> hienThiDanhSach() {
        return nhanVienRepository.findByTrangThaiTrue().stream()
                .map(NhanVienResponse::new)
                .toList();
    }
}
