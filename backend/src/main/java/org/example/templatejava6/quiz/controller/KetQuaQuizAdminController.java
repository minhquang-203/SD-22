package org.example.templatejava6.quiz.controller;

import org.example.templatejava6.quiz.model.response.KetQuaQuizAdminResponse;
import org.example.templatejava6.quiz.model.response.SanPhamGoiYResponse;
import org.example.templatejava6.quiz.service.KetQuaQuizAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ket-qua-quiz")
public class KetQuaQuizAdminController {

    @Autowired
    private KetQuaQuizAdminService ketQuaQuizAdminService;

    /** Lấy toàn bộ kết quả Quiz kèm thông tin khách hàng */
    @GetMapping
    public ResponseEntity<List<KetQuaQuizAdminResponse>> getAll() {
        return ResponseEntity.ok(ketQuaQuizAdminService.getAllKetQua());
    }

    /** Lấy sản phẩm gợi ý theo loại da */
    @GetMapping("/san-pham-goi-y/{idLoaiDa}")
    public ResponseEntity<List<SanPhamGoiYResponse>> getSanPhamGoiY(@PathVariable Integer idLoaiDa) {
        return ResponseEntity.ok(ketQuaQuizAdminService.getSanPhamGoiY(idLoaiDa));
    }
}
