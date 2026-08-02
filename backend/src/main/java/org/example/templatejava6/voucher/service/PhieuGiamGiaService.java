package org.example.templatejava6.voucher.service;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.voucher.model.request.PhieuGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaResponse;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaStatsResponse;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class PhieuGiamGiaService {

    final static int PAGE_SIZE = 10;
    final static int PAGE_OFFSET = 0;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Transactional(readOnly = true)
    public Page<PhieuGiamGiaResponse> getAll(Pageable pageable) {
        Page<PhieuGiamGia> phieuGiamGiaPage = phieuGiamGiaRepository.findByTrangThaiTrue(pageable);
                return phieuGiamGiaPage.map(PhieuGiamGiaResponse::new);
    }



    @Transactional(readOnly = true)
    public PhieuGiamGiaResponse detail(Integer id) {
        return new PhieuGiamGiaResponse(getPhieuGiamGiaOrThrow(id));
    }

    @Transactional
    public void add(PhieuGiamGiaRequest request) {
        normalizeRequest(request);
        validateRequest(request);
        if (phieuGiamGiaRepository.existsByMa(request.getMa())) {
            throw new ApiException("Mã phiếu giảm giá đã tồn tại", "DUPLICATE");
        }
        PhieuGiamGia pgg = MapperUtil.map(request, PhieuGiamGia.class);
        if (pgg.getGiaTriDonToiThieu() == null) {
            pgg.setGiaTriDonToiThieu(java.math.BigDecimal.ZERO);
        }
        pgg.setTrangThai(true);
        pgg.setIsActive(true);
        phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public void update(Integer id, PhieuGiamGiaRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        normalizeRequest(request);
        validateRequest(request);
        if (phieuGiamGiaRepository.existsByMaAndIdNot(request.getMa(), id)) {
            throw new ApiException("Mã phiếu giảm giá đã tồn tại", "DUPLICATE");
        }
        MapperUtil.mapToExisting(request, pgg);
        pgg.setId(id);
        if (pgg.getGiaTriDonToiThieu() == null) {
            pgg.setGiaTriDonToiThieu(java.math.BigDecimal.ZERO);
        }
        phieuGiamGiaRepository.save(pgg);
    }

    private void normalizeRequest(PhieuGiamGiaRequest request) {
        if (request.getMa() != null) {
            request.setMa(request.getMa().trim().toUpperCase());
        }
        if (request.getTen() != null) {
            request.setTen(request.getTen().trim());
        }
    }

    private void validateRequest(PhieuGiamGiaRequest request) {
        if (request.getLoai() == null) {
            throw new ApiException("Loại phiếu giảm giá không được để trống", "VALIDATION_ERROR");
        }
        if (request.getNgayBatDau() == null || request.getNgayKetThuc() == null) {
            throw new ApiException("Vui lòng chọn thời gian áp dụng", "VALIDATION_ERROR");
        }
        if (request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new ApiException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu", "VALIDATION_ERROR");
        }
        if (request.getSoLuong() == null || request.getSoLuong() < 1) {
            throw new ApiException("Số lượng phải lớn hơn hoặc bằng 1", "VALIDATION_ERROR");
        }
        if (request.getGiaTriDonToiThieu() != null
                && request.getGiaTriDonToiThieu().signum() < 0) {
            throw new ApiException("Đơn tối thiểu không hợp lệ", "VALIDATION_ERROR");
        }
        java.math.BigDecimal giaTri = request.getGiaTri();
        java.math.BigDecimal giamToiDa = request.getGiamToiDa();
        switch (request.getLoai()) {
            case PHAN_TRAM -> {
                if (giaTri == null || giaTri.signum() <= 0
                        || giaTri.compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                    throw new ApiException("Phần trăm giảm phải từ 1 đến 100", "VALIDATION_ERROR");
                }
                if (giamToiDa != null && giamToiDa.signum() <= 0) {
                    throw new ApiException("Giảm tối đa phải lớn hơn 0", "VALIDATION_ERROR");
                }
            }
            case TIEN_MAT -> {
                if (giaTri == null || giaTri.signum() <= 0) {
                    throw new ApiException("Số tiền giảm phải lớn hơn 0", "VALIDATION_ERROR");
                }
            }
            case FREE_SHIP -> {
                if (giamToiDa != null && giamToiDa.signum() <= 0) {
                    throw new ApiException("Miễn phí ship tối đa phải lớn hơn 0", "VALIDATION_ERROR");
                }
                // FREE_SHIP không dùng giaTri để tính; đặt giá trị mặc định hợp lệ cho cột NOT NULL.
                if (giaTri == null || giaTri.signum() <= 0) {
                    request.setGiaTri(java.math.BigDecimal.ONE);
                }
            }
        }
    }

    @Transactional
    public void delete(Integer id) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        pgg.setTrangThai(false);
        phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public void stop(Integer id) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        if (!Boolean.TRUE.equals(pgg.getTrangThai())) {
            throw new ApiException("Phiếu giảm giá không tồn tại", "NOT_FOUND");
        }
        if (!Boolean.TRUE.equals(pgg.getIsActive())) {
            throw new ApiException("Phiếu giảm giá đã ngừng áp dụng", "ALREADY_INACTIVE");
        }
        pgg.setIsActive(false);
        phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public void activate(Integer id) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        if (!Boolean.TRUE.equals(pgg.getTrangThai())) {
            throw new ApiException("Phiếu giảm giá không tồn tại", "NOT_FOUND");
        }
        if (Boolean.TRUE.equals(pgg.getIsActive())) {
            throw new ApiException("Phiếu giảm giá đang hoạt động", "ALREADY_ACTIVE");
        }
        pgg.setIsActive(true);
        phieuGiamGiaRepository.save(pgg);
    }

    @Transactional(readOnly = true)
    public PhieuGiamGia getPhieuGiamGiaOrThrow(Integer id) {
        return phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));
    }

    public Page<PhieuGiamGiaResponse> search(
            String keyword, String timeStatus, String loaipgg, Pageable pageable) {
        LoaiPhieuGiamGia loai = null;

        try {
            if (loaipgg != null && !loaipgg.isBlank()) {
                loai = LoaiPhieuGiamGia.valueOf(loaipgg);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Loại phiếu giảm giá không hợp lệ");
        }

        Page<PhieuGiamGia> phieuGiamGiaList = phieuGiamGiaRepository.search(keyword, timeStatus, loai, pageable);
        return  phieuGiamGiaList.map(PhieuGiamGiaResponse::new);
    }

    @Transactional(readOnly = true)
    public PhieuGiamGiaStatsResponse getStats() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        return new PhieuGiamGiaStatsResponse(
                phieuGiamGiaRepository.countActive(),
                hoaDonRepository.countVoucherUsage(),
                hoaDonRepository.sumVoucherSavings(),
                phieuGiamGiaRepository.countExpiringSoon(deadline)
        );
    }

    @Transactional(readOnly = true)
    public Page<PhieuGiamGiaResponse> listAvailableForCustomer(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        return phieuGiamGiaRepository.findAvailableForCustomer(normalizedKeyword, pageable)
                .map(PhieuGiamGiaResponse::new);
    }

//    public Page<PhieuGiamGiaResponse> paginition(return null)

}
