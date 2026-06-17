package org.example.templatejava6.product.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.LoHang;
import org.example.templatejava6.product.model.request.LoHangRequest;
import org.example.templatejava6.product.model.response.LoHangResponse;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.product.repository.LoHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class LoHangService {

    @Autowired private LoHangRepository loHangRepository;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Transactional(readOnly = true)
    public List<LoHangResponse> listByChiTiet(Integer idChiTietSanPham) {
        getChiTietOrThrow(idChiTietSanPham);
        return loHangRepository.findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(idChiTietSanPham)
                .stream()
                .map(LoHangResponse::new)
                .toList();
    }

    @Transactional
    public LoHangResponse nhapLo(LoHangRequest request) {
        ChiTietSanPham ct = getChiTietOrThrow(request.getIdChiTietSanPham());
        if (!Boolean.TRUE.equals(ct.getTrangThai())) {
            throw new ApiException("Biến thể không còn hoạt động", "INACTIVE_SKU");
        }
        if (request.getSoLuongNhap() == null || request.getSoLuongNhap() <= 0) {
            throw new ApiException("Số lượng nhập phải lớn hơn 0", "VALIDATION_ERROR");
        }

        LoHang lo = new LoHang();
        lo.setChiTietSanPham(ct);
        lo.setSoLo(request.getSoLo().trim());
        lo.setNgayNhap(request.getNgayNhap());
        lo.setHanSuDung(request.getHanSuDung());
        lo.setSoLuongNhap(request.getSoLuongNhap());
        lo.setSoLuongCon(request.getSoLuongNhap());
        lo.setGhiChu(request.getGhiChu());
        lo.setTrangThai(true);
        lo = loHangRepository.save(lo);

        syncTonKho(ct.getId());
        return new LoHangResponse(lo);
    }

    @Transactional
    public void truTonTheoFefo(Integer idChiTietSanPham, int soLuong) {
        if (soLuong <= 0) {
            return;
        }
        List<LoHang> lots = loHangRepository.findAvailableForFefo(idChiTietSanPham);
        int remaining = soLuong;
        for (LoHang lot : lots) {
            if (remaining <= 0) {
                break;
            }
            int available = lot.getSoLuongCon() != null ? lot.getSoLuongCon() : 0;
            int deduct = Math.min(available, remaining);
            lot.setSoLuongCon(available - deduct);
            loHangRepository.save(lot);
            remaining -= deduct;
        }
        if (remaining > 0) {
            ChiTietSanPham ct = getChiTietOrThrow(idChiTietSanPham);
            throw new ApiException(
                    "Không đủ tồn kho cho SKU " + ct.getSku() + " (thiếu " + remaining + ").",
                    "OUT_OF_STOCK");
        }
        syncTonKho(idChiTietSanPham);
    }

    @Transactional
    public void syncTonKho(Integer idChiTietSanPham) {
        ChiTietSanPham ct = getChiTietOrThrow(idChiTietSanPham);
        int total = loHangRepository.sumSoLuongCon(idChiTietSanPham);
        ct.setSoLuongTon(total);
        chiTietSanPhamRepository.save(ct);
    }

    @Transactional(readOnly = true)
    public LocalDate nearestExpiry(Integer idChiTietSanPham) {
        return loHangRepository.findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(idChiTietSanPham)
                .stream()
                .filter(l -> Boolean.TRUE.equals(l.getTrangThai()))
                .filter(l -> l.getSoLuongCon() != null && l.getSoLuongCon() > 0)
                .map(LoHang::getHanSuDung)
                .filter(d -> d != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean hasSapHetHan(Integer idChiTietSanPham) {
        return loHangRepository.findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(idChiTietSanPham)
                .stream()
                .anyMatch(l -> Boolean.TRUE.equals(l.getTrangThai())
                        && l.getSoLuongCon() != null
                        && l.getSoLuongCon() > 0
                        && LoHangResponse.isSapHetHan(l.getHanSuDung()));
    }

    private ChiTietSanPham getChiTietOrThrow(Integer id) {
        return chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
    }
}
