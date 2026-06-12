package org.example.templatejava6.voucher.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.example.templatejava6.voucher.model.request.DotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.DotGiamGiaResponse;
import org.example.templatejava6.voucher.repository.ChiTietDotGiamGiaRepository;
import org.example.templatejava6.voucher.repository.DotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DotGiamGiaService {

    @Autowired private DotGiamGiaRepository dotGiamGiaRepository;
    @Autowired private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    @Transactional(readOnly = true)
    public List<DotGiamGiaResponse> getAll() {
        return dotGiamGiaRepository.findByTrangThaiTrue().stream()
                .map(DotGiamGiaResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DotGiamGiaResponse detail(Integer id) {
        return new DotGiamGiaResponse(getDotGiamGiaOrThrow(id));
    }

    @Transactional
    public void add(DotGiamGiaRequest request) {
        if (dotGiamGiaRepository.existsByMa(request.getMa())) {
            throw new ApiException("Mã đợt giảm giá đã tồn tại", "DUPLICATE");
        }
        DotGiamGia dgg = MapperUtil.map(request, DotGiamGia.class);
        dgg.setTrangThai(true);
        dotGiamGiaRepository.save(dgg);
    }

    @Transactional
    public void update(Integer id, DotGiamGiaRequest request) {
        DotGiamGia dgg = getDotGiamGiaOrThrow(id);
        if (dotGiamGiaRepository.existsByMaAndIdNot(request.getMa(), id)) {
            throw new ApiException("Mã đợt giảm giá đã tồn tại", "DUPLICATE");
        }
        BigDecimal oldPhanTram = dgg.getPhanTramGiam();
        MapperUtil.mapToExisting(request, dgg);
        dgg.setId(id);
        dotGiamGiaRepository.save(dgg);

        if (request.getPhanTramGiam() != null && oldPhanTram != null
                && request.getPhanTramGiam().compareTo(oldPhanTram) != 0) {
            recalculateChiTietGiaSauGiam(dgg);
        }
    }

    @Transactional
    public void delete(Integer id) {
        DotGiamGia dgg = getDotGiamGiaOrThrow(id);
        dgg.setTrangThai(false);
        dotGiamGiaRepository.save(dgg);
    }

    @Transactional(readOnly = true)
    public DotGiamGia getDotGiamGiaOrThrow(Integer id) {
        return dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đợt giảm giá", "NOT_FOUND"));
    }

    public BigDecimal calculateGiaSauGiam(BigDecimal giaBan, BigDecimal phanTramGiam) {
        if (giaBan == null || phanTramGiam == null) {
            return null;
        }
        BigDecimal giam = giaBan.multiply(phanTramGiam)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return giaBan.subtract(giam);
    }

    private void recalculateChiTietGiaSauGiam(DotGiamGia dgg) {
        chiTietDotGiamGiaRepository.findByIdDotGiamGia(dgg).forEach(ct -> {
            if (ct.getIdChiTietSanPham() != null && ct.getIdChiTietSanPham().getGiaBan() != null) {
                ct.setGiaSauGiam(calculateGiaSauGiam(
                        ct.getIdChiTietSanPham().getGiaBan(), dgg.getPhanTramGiam()));
                chiTietDotGiamGiaRepository.save(ct);
            }
        });
    }
}
