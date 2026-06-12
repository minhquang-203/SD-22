package org.example.templatejava6.customer.controller;

import org.example.templatejava6.customer.model.request.TaoKhachNhanhRequest;
import org.example.templatejava6.customer.model.response.KhachHangDetailResponse;
import org.example.templatejava6.customer.model.response.KhachHangResponse;
import org.example.templatejava6.customer.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/khach-hang")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public List<KhachHangResponse> hienThiDanhSach() {
        return khachHangService.getAll();
    }

    @GetMapping("padding")
    public List<KhachHangResponse> phanTrang(@RequestParam("pageNo") Integer pageNo,
                                             @RequestParam("pageSize") Integer pageSize) {
        return khachHangService.phanTrang(pageNo, pageSize).getContent();
    }

    @GetMapping("tim")
    public List<KhachHangResponse> timKiem(@RequestParam("keyword") String keyword) {
        return khachHangService.timKiem(keyword);
    }

    @GetMapping("detail")
    public KhachHangDetailResponse detail(@RequestParam("id") Integer id) {
        return khachHangService.detail(id);
    }

    @PutMapping("trang-thai")
    public void capNhatTrangThai(@RequestParam("id") Integer id,
                                 @RequestParam("trangThai") Boolean trangThai) {
        khachHangService.updateTrangThai(id, trangThai);
    }

    @GetMapping("theo-sdt")
    public KhachHangResponse timTheoSoDienThoai(@RequestParam("sdt") String sdt) {
        return khachHangService.timTheoSoDienThoai(sdt);
    }

    @PostMapping("tao-nhanh")
    public KhachHangResponse taoNhanh(@RequestBody TaoKhachNhanhRequest request) {
        return khachHangService.taoNhanh(request);
    }
}
