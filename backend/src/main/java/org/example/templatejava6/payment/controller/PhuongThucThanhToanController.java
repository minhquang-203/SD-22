package org.example.templatejava6.payment.controller;

import org.example.templatejava6.payment.model.response.PhuongThucThanhToanResponse;
import org.example.templatejava6.order.repository.PhuongThucThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/phuong-thuc-thanh-toan")
public class PhuongThucThanhToanController {

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @GetMapping
    public List<PhuongThucThanhToanResponse> hienThiDanhSach() {
        return phuongThucThanhToanRepository.findByTrangThaiTrue().stream()
                .map(PhuongThucThanhToanResponse::new)
                .toList();
    }
}
