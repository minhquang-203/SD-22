package org.example.templatejava6.voucher.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.voucher.entity.ChiTietDotGiamGia;
import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.example.templatejava6.voucher.model.request.ChiTietDotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.ChiTietDotGiamGiaResponse;
import org.example.templatejava6.voucher.repository.ChiTietDotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChiTietDotGiamGiaService {

    @Autowired
    private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    @Autowired
    private DotGiamGiaService dotGiamGiaService;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Transactional(readOnly = true)
    public List<ChiTietDotGiamGiaResponse> getByDotGiamGia(Integer idDotGiamGia) {
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(idDotGiamGia);
        return chiTietDotGiamGiaRepository.findByIdDotGiamGia(dgg).stream()
                .map(ChiTietDotGiamGiaResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChiTietDotGiamGiaResponse detail(Integer id) {
        return new ChiTietDotGiamGiaResponse(getChiTietOrThrow(id));
    }

    @Transactional
    public void addToDotGiamGia(Integer idDotGiamGia, ChiTietDotGiamGiaRequest request) {
        if (request == null) {
            request = new ChiTietDotGiamGiaRequest();
        }
        request.setIdDotGiamGia(idDotGiamGia);
        add(request);
    }

    @Transactional
    public void updateInDotGiamGia(Integer idDotGiamGia, Integer id, ChiTietDotGiamGiaRequest request) {
        ChiTietDotGiamGia existing = getChiTietOrThrow(id);
        validateBelongsToDotGiamGia(existing, idDotGiamGia);
        if (request == null) {
            request = new ChiTietDotGiamGiaRequest();
        }
        request.setIdDotGiamGia(idDotGiamGia);
        update(id, request);
    }

    @Transactional
    public void deleteInDotGiamGia(Integer idDotGiamGia, Integer id) {
        ChiTietDotGiamGia existing = getChiTietOrThrow(id);
        validateBelongsToDotGiamGia(existing, idDotGiamGia);
        chiTietDotGiamGiaRepository.delete(existing);
    }

    @Transactional
    public void add(ChiTietDotGiamGiaRequest request) {
        validateRequiredIds(request);
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(request.getIdDotGiamGia());
        validateDotGiamGiaDangMo(dgg);
        ChiTietSanPham ctsp = getChiTietSanPhamOrThrow(request.getIdChiTietSanPham());
        validateDuplicate(dgg, ctsp.getId(), null);

        ChiTietDotGiamGia ct = new ChiTietDotGiamGia();
        ct.setIdDotGiamGia(dgg);
        ct.setIdChiTietSanPham(ctsp);
        ct.setGiaSauGiam(resolveGiaSauGiam(request, dgg, ctsp));
        chiTietDotGiamGiaRepository.save(ct);
    }

    @Transactional
    public void update(Integer id, ChiTietDotGiamGiaRequest request) {
        ChiTietDotGiamGia ct = getChiTietOrThrow(id);
        validateRequiredIds(request);
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(request.getIdDotGiamGia());
        validateDotGiamGiaDangMo(dgg);
        ChiTietSanPham ctsp = getChiTietSanPhamOrThrow(request.getIdChiTietSanPham());
        validateDuplicate(dgg, ctsp.getId(), id);

        ct.setIdDotGiamGia(dgg);
        ct.setIdChiTietSanPham(ctsp);
        ct.setGiaSauGiam(resolveGiaSauGiam(request, dgg, ctsp));
        ct.setId(id);
        chiTietDotGiamGiaRepository.save(ct);
    }

    @Transactional
    public void delete(Integer id) {
        ChiTietDotGiamGia ct = getChiTietOrThrow(id);
        chiTietDotGiamGiaRepository.delete(ct);
    }

    private ChiTietDotGiamGia getChiTietOrThrow(Integer id) {
        return chiTietDotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết đợt giảm giá", "NOT_FOUND"));
    }

    private ChiTietSanPham getChiTietSanPhamOrThrow(Integer id) {
        return chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
    }

    private void validateDuplicate(DotGiamGia dgg, Integer idChiTietSanPham, Integer excludeId) {
        boolean exists = excludeId == null
                ? chiTietDotGiamGiaRepository.existsByIdDotGiamGiaAndIdChiTietSanPham_Id(dgg, idChiTietSanPham)
                : chiTietDotGiamGiaRepository.existsByIdDotGiamGiaAndIdChiTietSanPham_IdAndIdNot(
                dgg, idChiTietSanPham, excludeId);
        if (exists) {
            throw new ApiException("Sản phẩm đã tồn tại trong đợt giảm giá này", "DUPLICATE");
        }
    }

    private void validateRequiredIds(ChiTietDotGiamGiaRequest request) {
        if (request == null) {
            throw new ApiException("Dữ liệu sản phẩm giảm giá không được để trống", "VALIDATION_ERROR");
        }
        if (request.getIdDotGiamGia() == null) {
            throw new ApiException("Đợt giảm giá không được để trống", "VALIDATION_ERROR");
        }
        if (request.getIdChiTietSanPham() == null) {
            throw new ApiException("Chi tiết sản phẩm không được để trống", "VALIDATION_ERROR");
        }
    }

    private void validateBelongsToDotGiamGia(ChiTietDotGiamGia ct, Integer idDotGiamGia) {
        if (ct.getIdDotGiamGia() == null || !ct.getIdDotGiamGia().getId().equals(idDotGiamGia)) {
            throw new ApiException("Sản phẩm không thuộc đợt giảm giá này", "VALIDATION_ERROR");
        }
    }

    private BigDecimal resolveGiaSauGiam(
            ChiTietDotGiamGiaRequest request, DotGiamGia dgg, ChiTietSanPham ctsp) {
        if (request.getGiaSauGiam() != null) {
            validateGiaSauGiam(request.getGiaSauGiam(), ctsp.getGiaBan());
            return request.getGiaSauGiam();
        }
        return dotGiamGiaService.calculateGiaSauGiam(ctsp.getGiaBan(), dgg.getPhanTramGiam());
    }

    private void validateDotGiamGiaDangMo(DotGiamGia dgg) {
        if (!Boolean.TRUE.equals(dgg.getTrangThai())) {
            throw new ApiException("Đợt giảm giá đã ngừng áp dụng", "VALIDATION_ERROR");
        }
    }

    private void validateGiaSauGiam(BigDecimal giaSauGiam, BigDecimal giaBan) {
        if (giaBan != null && giaSauGiam.compareTo(giaBan) >= 0) {
            throw new ApiException("Giá sau giảm phải nhỏ hơn giá bán", "VALIDATION_ERROR");
        }
    }
}
