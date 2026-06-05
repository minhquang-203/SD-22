package org.example.templatejava6.review.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.review.model.request.DanhGiaRequest;
import org.example.templatejava6.review.model.response.DanhGiaResponse;
import org.example.templatejava6.review.service.DanhGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/danh-gia")
public class DanhGiaController {

    @Autowired
    private DanhGiaService danhGiaService;

    @GetMapping("san-pham")
    public List<DanhGiaResponse> getBySanPham(@RequestParam("idSanPham") Integer idSanPham) {
        return danhGiaService.getApprovedBySanPham(idSanPham);
    }

    @GetMapping("cho-duyet")
    public List<DanhGiaResponse> getChoDuyet() {
        return danhGiaService.getChoDuyet();
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody DanhGiaRequest request) {
        danhGiaService.add(request);
    }

    @PutMapping("duyet/{id}")
    public void duyet(@PathVariable("id") Integer id, @RequestParam("trangThai") String trangThai) {
        danhGiaService.duyet(id, trangThai);
    }
}
