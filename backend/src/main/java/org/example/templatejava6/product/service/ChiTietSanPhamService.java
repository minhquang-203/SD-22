package org.example.templatejava6.product.service;

import org.example.templatejava6.category.service.CategoryService;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.model.request.ChiTietSanPhamRequest;
import org.example.templatejava6.product.model.response.ChiTietSanPhamResponse;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ChiTietSanPhamService {

    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private SanPhamService sanPhamService;
    @Autowired private CategoryService categoryService;

    @Transactional
    public void add(ChiTietSanPhamRequest request) {
        if (request.getIdSanPham() == null) {
            throw new ApiException("Id sản phẩm không được để trống", "VALIDATION_ERROR");
        }
        validateGia(request.getGiaBan());
        validateSku(request.getSku(), null);
        ChiTietSanPham ct = MapperUtil.map(request, ChiTietSanPham.class);
        ct.setSanPham(sanPhamService.getSanPhamOrThrow(request.getIdSanPham()));
        ct.setMauSac(categoryService.getMauSacOrNull(request.getIdMauSac()));
        ct.setTrangThai(true);
        if (ct.getSoLuongTon() == null) {
            ct.setSoLuongTon(0);
        }
        chiTietSanPhamRepository.save(ct);
    }

    @Transactional
    public void update(Integer id, ChiTietSanPhamRequest request) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
        validateGia(request.getGiaBan());
        validateSku(request.getSku(), id);
        MapperUtil.mapToExisting(request, ct);
        if (request.getIdSanPham() != null) {
            ct.setSanPham(sanPhamService.getSanPhamOrThrow(request.getIdSanPham()));
        }
        ct.setMauSac(categoryService.getMauSacOrNull(request.getIdMauSac()));
        ct.setId(id);
        chiTietSanPhamRepository.save(ct);
    }

    public void delete(Integer id) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
        ct.setTrangThai(false);
        chiTietSanPhamRepository.save(ct);
    }

    @Transactional(readOnly = true)
    public ChiTietSanPhamResponse detail(Integer id) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
        return new ChiTietSanPhamResponse(ct);
    }

    private void validateGia(BigDecimal giaBan) {
        if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Giá bán phải lớn hơn 0", "VALIDATION_ERROR");
        }
    }

    private void validateSku(String sku, Integer excludeId) {
        chiTietSanPhamRepository.findBySku(sku).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new ApiException("SKU đã tồn tại: " + sku, "DUPLICATE");
            }
        });
    }
}
