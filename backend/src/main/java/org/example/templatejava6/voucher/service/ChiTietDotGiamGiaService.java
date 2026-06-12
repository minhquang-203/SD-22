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
    public void add(ChiTietDotGiamGiaRequest request) {
        if (request.getIdDotGiamGia() == null) {
            throw new ApiException("Id đợt giảm giá không được để trống", "VALIDATION_ERROR");
        }
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(request.getIdDotGiamGia());
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
        DotGiamGia dgg = ct.getIdDotGiamGia();
        ChiTietSanPham ctsp = ct.getIdChiTietSanPham();

        if (request.getIdDotGiamGia() != null) {
            dgg = dotGiamGiaService.getDotGiamGiaOrThrow(request.getIdDotGiamGia());
            ct.setIdDotGiamGia(dgg);
        }
        if (request.getIdChiTietSanPham() != null) {
            ctsp = getChiTietSanPhamOrThrow(request.getIdChiTietSanPham());
            validateDuplicate(dgg, ctsp.getId(), id);
            ct.setIdChiTietSanPham(ctsp);
        }
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

    private java.math.BigDecimal resolveGiaSauGiam(
            ChiTietDotGiamGiaRequest request, DotGiamGia dgg, ChiTietSanPham ctsp) {
        if (request.getGiaSauGiam() != null) {
            return request.getGiaSauGiam();
        }
        return dotGiamGiaService.calculateGiaSauGiam(ctsp.getGiaBan(), dgg.getPhanTramGiam());
    }
}
