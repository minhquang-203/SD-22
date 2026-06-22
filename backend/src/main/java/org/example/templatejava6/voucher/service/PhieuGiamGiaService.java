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
        if (phieuGiamGiaRepository.existsByMa(request.getMa())) {
            throw new ApiException("Mã phiếu giảm giá đã tồn tại", "DUPLICATE");
        }
        PhieuGiamGia pgg = MapperUtil.map(request, PhieuGiamGia.class);
        if (pgg.getGiaTriDonToiThieu() == null) {
            pgg.setGiaTriDonToiThieu(java.math.BigDecimal.ZERO);
        }
        pgg.setTrangThai(true);
        phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public void update(Integer id, PhieuGiamGiaRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        if (phieuGiamGiaRepository.existsByMaAndIdNot(request.getMa(), id)) {
            throw new ApiException("Mã phiếu giảm giá đã tồn tại", "DUPLICATE");
        }
        MapperUtil.mapToExisting(request, pgg);
        pgg.setId(id);
        phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public void delete(Integer id) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        pgg.setTrangThai(false);
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

//    public Page<PhieuGiamGiaResponse> paginition(return null)

}
