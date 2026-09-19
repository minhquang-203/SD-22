package org.example.templatejava6.wishlist.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.product.service.SanPhamService;
import org.example.templatejava6.wishlist.entity.SanPhamYeuThich;
import org.example.templatejava6.wishlist.model.request.YeuThichRequest;
import org.example.templatejava6.wishlist.model.response.YeuThichResponse;
import org.example.templatejava6.wishlist.repository.SanPhamYeuThichRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class YeuThichService {

    @Autowired private SanPhamYeuThichRepository yeuThichRepository;
    @Autowired private SanPhamService sanPhamService;

    @Transactional(readOnly = true)
    public List<YeuThichResponse> danhSachCuaToi() {
        Integer idKhachHang = SecurityUtils.currentKhachHangId();
        return yeuThichRepository.findByIdKhachHang(idKhachHang)
                .stream().map(YeuThichResponse::new).toList();
    }

    @Transactional
    public void add(YeuThichRequest request) {
        Integer idKhachHang = SecurityUtils.currentKhachHangId();
        if (yeuThichRepository.existsByIdKhachHangAndSanPham_Id(idKhachHang, request.getIdSanPham())) {
            throw new ApiException("Sản phẩm đã có trong danh sách yêu thích", "DUPLICATE");
        }
        SanPhamYeuThich yt = new SanPhamYeuThich();
        yt.setIdKhachHang(idKhachHang);
        yt.setSanPham(sanPhamService.getSanPhamOrThrow(request.getIdSanPham()));
        yt.setNgayThem(LocalDateTime.now());
        yeuThichRepository.save(yt);
    }

    /** Chỉ xóa được mục thuộc chính khách đang đăng nhập. */
    @Transactional
    public void deleteCuaToi(Integer id) {
        Integer idKhachHang = SecurityUtils.currentKhachHangId();
        SanPhamYeuThich yt = yeuThichRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy mục yêu thích", "NOT_FOUND"));
        if (!idKhachHang.equals(yt.getIdKhachHang())) {
            throw new ApiException("Bạn không có quyền xóa mục yêu thích này", "FORBIDDEN");
        }
        yeuThichRepository.delete(yt);
    }
}
