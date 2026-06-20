package org.example.templatejava6.product.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.common.model.response.MaTiepTheoResponse;
import org.example.templatejava6.product.model.request.SanPhamRequest;
import org.example.templatejava6.product.model.response.SanPhamDetailResponse;
import org.example.templatejava6.product.model.response.SanPhamResponse;
import org.example.templatejava6.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public List<SanPhamResponse> hienThiDanhSach() {
        return sanPhamService.getAll();
    }

    @GetMapping("padding")
    public List<SanPhamResponse> phanTrang(@RequestParam("pageNo") Integer pageNo,
                                           @RequestParam("pageSize") Integer pageSize) {
        return sanPhamService.phanTrang(pageNo, pageSize).getContent();
    }

    @GetMapping("detail")
    public SanPhamDetailResponse detail(@RequestParam("id") Integer id) {
        return sanPhamService.detail(id);
    }

    @GetMapping("ma-tiep-theo")
    public MaTiepTheoResponse maTiepTheo() {
        return sanPhamService.previewMaTiepTheo();
    }

    @GetMapping("tim")
    public List<SanPhamResponse> timKiem(@RequestParam("keyword") String keyword) {
        return sanPhamService.timKiem(keyword);
    }

    @PostMapping(value = "add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void add(@RequestPart("data") @Valid SanPhamRequest request,
                    @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        sanPhamService.add(request, files);
    }

    @PostMapping(value = "update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(@RequestPart("data") @Valid SanPhamRequest request,
                       @PathVariable("id") Integer id,
                       @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        sanPhamService.update(id, request, files);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        sanPhamService.delete(id);
    }

    @PutMapping("trang-thai")
    public void capNhatTrangThai(@RequestParam("id") Integer id,
                                   @RequestParam("trangThai") Boolean trangThai) {
        sanPhamService.updateTrangThai(id, trangThai);
    }

    @PutMapping("noi-bat")
    public void capNhatNoiBat(@RequestParam("id") Integer id,
                              @RequestParam("noiBat") Boolean noiBat) {
        sanPhamService.updateNoiBat(id, noiBat);
    }
}
