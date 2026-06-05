package org.example.templatejava6.wishlist.service;

import org.example.templatejava6.common.exception.ApiException;
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
    public List<YeuThichResponse> getByKhachHang(Integer idKhachHang) {
        return yeuThichRepository.findByIdKhachHang(idKhachHang)
                .stream().map(YeuThichResponse::new).toList();
    }

    @Transactional
    public void add(YeuThichRequest request) {
        if (yeuThichRepository.existsByIdKhachHangAndSanPham_Id(
                request.getIdKhachHang(), request.getIdSanPham())) {
            throw new ApiException("Sản phẩm đã có trong danh sách yêu thích", "DUPLICATE");
        }
        SanPhamYeuThich yt = new SanPhamYeuThich();
        yt.setIdKhachHang(request.getIdKhachHang());
        yt.setSanPham(sanPhamService.getSanPhamOrThrow(request.getIdSanPham()));
        yt.setNgayThem(LocalDateTime.now());
        yeuThichRepository.save(yt);
    }

    @Transactional
    public void delete(Integer id) {
        SanPhamYeuThich yt = yeuThichRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy mục yêu thích", "NOT_FOUND"));
        yeuThichRepository.delete(yt);
    }
}
