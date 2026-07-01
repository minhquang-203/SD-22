package org.example.templatejava6.voucher.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.example.templatejava6.voucher.model.request.DotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.DotGiamGiaResponse;
import org.example.templatejava6.voucher.model.response.VariantSaleInfo;
import org.example.templatejava6.voucher.repository.ChiTietDotGiamGiaRepository;
import org.example.templatejava6.voucher.repository.DotGiamGiaRepository;
import org.example.templatejava6.voucher.repository.VariantSalePriceRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Transactional(readOnly = true)
    public Page<DotGiamGiaResponse> search(String keyword, String timeStatus, Pageable pageable) {
        String normalizedStatus = normalizeTimeStatus(timeStatus);
        return dotGiamGiaRepository.search(normalizeKeyword(keyword), normalizedStatus, pageable)
                .map(DotGiamGiaResponse::new);
    }

    @Transactional
    public void add(DotGiamGiaRequest request) {
        normalizeRequest(request);
        validateRequest(request);
        if (dotGiamGiaRepository.existsByMa(request.getMa())) {
            throw new ApiException("Mã đợt giảm giá đã tồn tại", "DUPLICATE");
        }
        DotGiamGia dgg = MapperUtil.map(request, DotGiamGia.class);
        dgg.setTrangThai(true);
        dgg.setIsActive(true);
        dotGiamGiaRepository.save(dgg);
    }

    @Transactional
    public void update(Integer id, DotGiamGiaRequest request) {
        DotGiamGia dgg = getDotGiamGiaOrThrow(id);
        normalizeRequest(request);
        validateRequest(request);
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

    @Transactional
    public void stop(Integer id) {
        DotGiamGia dgg = getDotGiamGiaOrThrow(id);
        if (!Boolean.TRUE.equals(dgg.getTrangThai())) {
            throw new ApiException("Đợt giảm giá không tồn tại", "NOT_FOUND");
        }
        if (!Boolean.TRUE.equals(dgg.getIsActive())) {
            throw new ApiException("Đợt giảm giá đã ngừng áp dụng", "ALREADY_INACTIVE");
        }
        dgg.setIsActive(false);
        dotGiamGiaRepository.save(dgg);
    }

    @Transactional
    public void activate(Integer id) {
        DotGiamGia dgg = getDotGiamGiaOrThrow(id);
        if (!Boolean.TRUE.equals(dgg.getTrangThai())) {
            throw new ApiException("Đợt giảm giá không tồn tại", "NOT_FOUND");
        }
        if (Boolean.TRUE.equals(dgg.getIsActive())) {
            throw new ApiException("Đợt giảm giá đang hoạt động", "ALREADY_ACTIVE");
        }
        dgg.setIsActive(true);
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

    public boolean isDangApDung(DotGiamGia dgg) {
        if (dgg == null || !Boolean.TRUE.equals(dgg.getTrangThai()) || !Boolean.TRUE.equals(dgg.getIsActive())) {
            return false;
        }
        if (dgg.getNgayBatDau() == null || dgg.getNgayKetThuc() == null) {
            return false;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return !dgg.getNgayBatDau().isAfter(now) && !dgg.getNgayKetThuc().isBefore(now);
    }

    @Transactional(readOnly = true)
    public Map<Integer, VariantSaleInfo> getActiveSaleByVariantId() {
        Map<Integer, VariantSaleInfo> map = new HashMap<>();
        for (VariantSalePriceRow row : chiTietDotGiamGiaRepository.findActiveVariantSaleRows()) {
            VariantSaleInfo info = resolveVariantSaleInfo(row);
            if (info == null || row.getChiTietSanPhamId() == null) {
                continue;
            }
            map.merge(row.getChiTietSanPhamId(), info, (current, incoming) ->
                    incoming.getGiaSauGiam().compareTo(current.getGiaSauGiam()) < 0 ? incoming : current);
        }
        return map;
    }

    public VariantSaleInfo resolveVariantSaleInfo(VariantSalePriceRow row) {
        if (row == null) {
            return null;
        }
        BigDecimal giaGoc = row.getGiaGoc();
        BigDecimal phanTramGiam = row.getPhanTramGiam();
        BigDecimal giaSauGiam = row.getGiaSauGiam();
        if (giaSauGiam == null && giaGoc != null && phanTramGiam != null) {
            giaSauGiam = calculateGiaSauGiam(giaGoc, phanTramGiam);
        }
        if (giaGoc == null || giaSauGiam == null || giaSauGiam.compareTo(giaGoc) >= 0) {
            return null;
        }
        return new VariantSaleInfo(giaGoc, giaSauGiam, phanTramGiam);
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

    private void normalizeRequest(DotGiamGiaRequest request) {
        if (request.getMa() != null) {
            request.setMa(request.getMa().trim());
        }
        if (request.getTen() != null) {
            request.setTen(request.getTen().trim());
        }
    }

    private void validateRequest(DotGiamGiaRequest request) {
        if (request.getPhanTramGiam() != null
                && request.getPhanTramGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException("Phần trăm giảm không được vượt quá 100", "VALIDATION_ERROR");
        }
        if (request.getNgayBatDau() == null) {
            throw new ApiException("Ngày bắt đầu không được để trống", "VALIDATION_ERROR");
        }
        if (request.getNgayKetThuc() == null) {
            throw new ApiException("Ngày kết thúc không được để trống", "VALIDATION_ERROR");
        }
        if (!request.getNgayKetThuc().isAfter(request.getNgayBatDau())) {
            throw new ApiException("Ngày kết thúc phải sau ngày bắt đầu", "VALIDATION_ERROR");
        }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? null : keyword.trim();
    }

    private String normalizeTimeStatus(String timeStatus) {
        if (timeStatus == null || timeStatus.isBlank()) {
            return null;
        }
        String normalized = timeStatus.trim().toUpperCase();
        if (!List.of("ACTIVE", "UPCOMING", "EXPIRED", "INACTIVE").contains(normalized)) {
            throw new ApiException("Trạng thái thời gian không hợp lệ", "VALIDATION_ERROR");
        }
        return normalized;
    }
}
