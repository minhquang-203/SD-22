package org.example.templatejava6.cart.service;

import org.example.templatejava6.cart.entity.ChiTietGioHang;
import org.example.templatejava6.cart.entity.GioHang;
import org.example.templatejava6.cart.model.request.CapNhatSoLuongGioHangRequest;
import org.example.templatejava6.cart.model.request.ThemGioHangRequest;
import org.example.templatejava6.cart.model.response.ChiTietGioHangResponse;
import org.example.templatejava6.cart.model.response.GioHangResponse;
import org.example.templatejava6.cart.repository.ChiTietGioHangRepository;
import org.example.templatejava6.cart.repository.GioHangRepository;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.repository.AnhSanPhamRepository;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GioHangService {

    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private ChiTietGioHangRepository chiTietGioHangRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private AnhSanPhamRepository anhSanPhamRepository;

    @Transactional
    public GioHangResponse getByKhachHang(Integer idKhachHang) {
        GioHang gioHang = getOrCreateGioHang(idKhachHang);
        return toResponse(gioHang);
    }

    @Transactional
    public GioHangResponse add(ThemGioHangRequest request) {
        GioHang gioHang = getOrCreateGioHang(request.getIdKhachHang());
        ChiTietSanPham chiTietSanPham = getActiveChiTietSanPham(request.getIdChiTietSanPham());

        ChiTietGioHang item = chiTietGioHangRepository
                .findByGioHang_IdAndChiTietSanPham_Id(gioHang.getId(), chiTietSanPham.getId())
                .orElseGet(() -> {
                    ChiTietGioHang newItem = new ChiTietGioHang();
                    newItem.setGioHang(gioHang);
                    newItem.setChiTietSanPham(chiTietSanPham);
                    newItem.setSoLuong(0);
                    return newItem;
                });

        int soLuongMoi = safeQuantity(item.getSoLuong()) + request.getSoLuong();
        validateStock(chiTietSanPham, soLuongMoi);
        item.setSoLuong(soLuongMoi);
        chiTietGioHangRepository.save(item);
        return toResponse(gioHang);
    }

    @Transactional
    public GioHangResponse updateSoLuong(Integer idKhachHang, Integer idChiTietGioHang,
                                         CapNhatSoLuongGioHangRequest request) {
        ChiTietGioHang item = chiTietGioHangRepository
                .findByIdAndGioHang_KhachHang_Id(idChiTietGioHang, idKhachHang)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm trong giỏ hàng", "NOT_FOUND"));
        ChiTietSanPham chiTietSanPham = getActiveChiTietSanPham(item.getChiTietSanPham().getId());
        validateStock(chiTietSanPham, request.getSoLuong());
        item.setSoLuong(request.getSoLuong());
        chiTietGioHangRepository.save(item);
        return toResponse(item.getGioHang());
    }

    @Transactional
    public GioHangResponse deleteItem(Integer idKhachHang, Integer idChiTietGioHang) {
        ChiTietGioHang item = chiTietGioHangRepository
                .findByIdAndGioHang_KhachHang_Id(idChiTietGioHang, idKhachHang)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm trong giỏ hàng", "NOT_FOUND"));
        GioHang gioHang = item.getGioHang();
        chiTietGioHangRepository.delete(item);
        return toResponse(gioHang);
    }

    @Transactional
    public GioHangResponse clear(Integer idKhachHang) {
        GioHang gioHang = findGioHang(idKhachHang);
        chiTietGioHangRepository.deleteByGioHang_Id(gioHang.getId());
        return toResponse(gioHang);
    }

    private GioHang findGioHang(Integer idKhachHang) {
        validateKhachHang(idKhachHang);
        return gioHangRepository.findFirstByKhachHang_IdOrderByIdAsc(idKhachHang)
                .orElseThrow(() -> new ApiException("Khách hàng chưa có giỏ hàng", "NOT_FOUND"));
    }

    private GioHang getOrCreateGioHang(Integer idKhachHang) {
        KhachHang khachHang = validateKhachHang(idKhachHang);
        return gioHangRepository.findFirstByKhachHang_IdOrderByIdAsc(idKhachHang)
                .orElseGet(() -> {
                    GioHang gioHang = new GioHang();
                    gioHang.setKhachHang(khachHang);
                    gioHang.setNgayTao(LocalDateTime.now());
                    return gioHangRepository.save(gioHang);
                });
    }

    private KhachHang validateKhachHang(Integer idKhachHang) {
        if (idKhachHang == null) {
            throw new ApiException("Id khách hàng không được để trống", "VALIDATION_ERROR");
        }
        return khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new ApiException("Khách hàng không tồn tại", "NOT_FOUND"));
    }

    private ChiTietSanPham getActiveChiTietSanPham(Integer idChiTietSanPham) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                .orElseThrow(() -> new ApiException("Biến thể sản phẩm không tồn tại", "NOT_FOUND"));
        if (!Boolean.TRUE.equals(chiTietSanPham.getTrangThai())) {
            throw new ApiException("SKU " + chiTietSanPham.getSku() + " không còn bán", "INACTIVE_SKU");
        }
        if (chiTietSanPham.getSanPham() != null && !Boolean.TRUE.equals(chiTietSanPham.getSanPham().getTrangThai())) {
            throw new ApiException("Sản phẩm không còn bán", "INACTIVE_PRODUCT");
        }
        return chiTietSanPham;
    }

    private void validateStock(ChiTietSanPham chiTietSanPham, Integer soLuong) {
        int ton = chiTietSanPham.getSoLuongTon() != null ? chiTietSanPham.getSoLuongTon() : 0;
        if (soLuong == null || soLuong <= 0) {
            throw new ApiException("Số lượng sản phẩm không hợp lệ", "INVALID_QTY");
        }
        if (ton < soLuong) {
            throw new ApiException(
                    "Không đủ tồn cho SKU " + chiTietSanPham.getSku() + " (còn " + ton + ").",
                    "OUT_OF_STOCK");
        }
    }

    private int safeQuantity(Integer soLuong) {
        return soLuong != null ? soLuong : 0;
    }

    private GioHangResponse toResponse(GioHang gioHang) {
        List<ChiTietGioHangResponse> items = chiTietGioHangRepository
                .findByGioHangIdWithDetail(gioHang.getId())
                .stream()
                .map(item -> new ChiTietGioHangResponse(item, resolveImageUrl(item.getChiTietSanPham())))
                .toList();
        return new GioHangResponse(gioHang, items);
    }

    private String resolveImageUrl(ChiTietSanPham chiTietSanPham) {
        if (chiTietSanPham == null || chiTietSanPham.getSanPham() == null) {
            return null;
        }
        Integer sanPhamId = chiTietSanPham.getSanPham().getId();
        return anhSanPhamRepository.findFirstBySanPham_IdAndLaAnhChinhTrue(sanPhamId)
                .map(AnhSanPham::getUrl)
                .or(() -> anhSanPhamRepository.findFirstBySanPham_IdOrderByThuTuAsc(sanPhamId)
                        .map(AnhSanPham::getUrl))
                .orElse(null);
    }
}
