package org.example.templatejava6.voucher.service;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.example.templatejava6.common.enums.PhamViPhieuGiamGia;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.voucher.model.request.PhieuGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaResponse;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaStatsResponse;
import org.example.templatejava6.voucher.repository.KhachHangPhieuGiamGiaRepository;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class PhieuGiamGiaService {

    final static int PAGE_SIZE = 10;
    final static int PAGE_OFFSET = 0;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private KhachHangPhieuGiamGiaRepository khachHangPhieuGiamGiaRepository;

    @Transactional(readOnly = true)
    public Page<PhieuGiamGiaResponse> getAll(Pageable pageable) {
        Page<PhieuGiamGia> phieuGiamGiaPage = phieuGiamGiaRepository.findByTrangThaiTrue(pageable);
        Page<PhieuGiamGiaResponse> mapped = phieuGiamGiaPage.map(PhieuGiamGiaResponse::new);
        attachUsage(mapped.getContent());
        return mapped;
    }

    /** Gắn số lượt đã dùng cho từng phiếu dựa trên số hóa đơn đã áp dụng. */
    private void attachUsage(java.util.List<PhieuGiamGiaResponse> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        java.util.List<Integer> ids = items.stream()
                .map(PhieuGiamGiaResponse::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        java.util.Map<Integer, Long> usageMap = new java.util.HashMap<>();
        for (Object[] row : hoaDonRepository.countUsageByVoucherIds(ids)) {
            if (row[0] != null) {
                usageMap.put((Integer) row[0], ((Number) row[1]).longValue());
            }
        }
        items.forEach(item -> item.setDaDung(usageMap.getOrDefault(item.getId(), 0L)));
    }



    @Transactional(readOnly = true)
    public PhieuGiamGiaResponse detail(Integer id) {
        return new PhieuGiamGiaResponse(getPhieuGiamGiaOrThrow(id));
    }

    @Transactional(readOnly = true)
    public String previewNextMa() {
        try {
            return MaGenerator.randomVoucherCode(6, phieuGiamGiaRepository::existsByMa);
        } catch (IllegalStateException ex) {
            throw new ApiException("Không sinh được mã phiếu giảm giá duy nhất", "CODE_GENERATE_FAILED");
        }
    }

    @Transactional
    public void add(PhieuGiamGiaRequest request) {
        normalizeRequest(request);
        validateRequest(request, true);
        String ma = resolveCreateMa(request.getMa());
        request.setMa(ma);
        PhieuGiamGia pgg = MapperUtil.map(request, PhieuGiamGia.class);
        if (pgg.getGiaTriDonToiThieu() == null) {
            pgg.setGiaTriDonToiThieu(java.math.BigDecimal.ZERO);
        }
        if (pgg.getPhamVi() == null) {
            pgg.setPhamVi(org.example.templatejava6.common.enums.PhamViPhieuGiamGia.CONG_KHAI);
        }
        pgg.setMa(ma);
        pgg.setTrangThai(true);
        pgg.setIsActive(true);
        phieuGiamGiaRepository.save(pgg);
    }

    /** Dùng mã đã xem trước nếu còn hợp lệ; không thì sinh mới. */
    private String resolveCreateMa(String requested) {
        if (requested != null
                && requested.matches("^SNO-[A-Z0-9]{6}$")
                && !phieuGiamGiaRepository.existsByMa(requested)) {
            return requested;
        }
        try {
            return MaGenerator.randomVoucherCode(6, phieuGiamGiaRepository::existsByMa);
        } catch (IllegalStateException ex) {
            throw new ApiException("Không sinh được mã phiếu giảm giá duy nhất", "CODE_GENERATE_FAILED");
        }
    }

    @Transactional
    public void update(Integer id, PhieuGiamGiaRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaOrThrow(id);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ngayBatDau  =pgg.getNgayBatDau();
        LocalDateTime ngayKetThuc =pgg.getNgayKetThuc();

        if(ngayBatDau != null && ngayKetThuc != null &&
                !now.isBefore(ngayBatDau) && !now.isAfter(ngayKetThuc)){
            throw new ApiException("Không thể cập nhật phiếu giảm giá đang trong thời gian hoạt động", "PROMOTION_ISACTIVE");
        }
        if(ngayKetThuc != null && ngayKetThuc.isBefore(now)){
            throw new ApiException("Không thể cập nhật phiếu giảm giá đã kết thúc", "PROMOTION_IS_OVER");
        }
        normalizeRequest(request);
        validateRequest(request, false);
        String maCu = pgg.getMa();
        MapperUtil.mapToExisting(request, pgg);
        pgg.setId(id);
        pgg.setMa(maCu);
        if (pgg.getGiaTriDonToiThieu() == null) {
            pgg.setGiaTriDonToiThieu(java.math.BigDecimal.ZERO);
        }
        if (pgg.getPhamVi() == null) {
            pgg.setPhamVi(org.example.templatejava6.common.enums.PhamViPhieuGiamGia.CONG_KHAI);
        }
        phieuGiamGiaRepository.save(pgg);
    }

    /**
     * Trừ 1 lượt dùng phiếu một cách atomic để tránh race condition khi nhiều đơn cùng lúc.
     */
    @Transactional
    public void consumeOne(Integer id) {
        if (id == null) {
            return;
        }
        int updated = phieuGiamGiaRepository.decrementSoLuongIfAvailable(id);
        if (updated == 0) {
            throw new ApiException("Mã giảm giá đã hết lượt sử dụng.", "INVALID_VOUCHER");
        }
    }

    /**
     * Hoàn lại 1 lượt dùng phiếu (khi hủy đơn / thanh toán thất bại).
     */
    @Transactional
    public void restoreOne(Integer id) {
        if (id == null) {
            return;
        }
        phieuGiamGiaRepository.incrementSoLuong(id);
    }

    private void normalizeRequest(PhieuGiamGiaRequest request) {
        if (request.getMa() != null) {
            request.setMa(request.getMa().trim().toUpperCase());
        }
        if (request.getTen() != null) {
            request.setTen(request.getTen().trim());
        }
    }

    private void validateRequest(PhieuGiamGiaRequest request, boolean isCreate) {
        if (request.getLoai() == null) {
            throw new ApiException("Loại phiếu giảm giá không được để trống", "VALIDATION_ERROR");
        }
        if (request.getNgayBatDau() == null || request.getNgayKetThuc() == null) {
            throw new ApiException("Vui lòng chọn thời gian áp dụng", "VALIDATION_ERROR");
        }
        if (request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new ApiException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu", "VALIDATION_ERROR");
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        if (isCreate && request.getNgayBatDau().toLocalDate().isBefore(today)) {
            throw new ApiException("Ngày bắt đầu không được nhỏ hơn ngày hiện tại", "VALIDATION_ERROR");
        }
        if (request.getNgayKetThuc().toLocalDate().isBefore(today)) {
            throw new ApiException("Ngày kết thúc không được nhỏ hơn ngày hiện tại", "VALIDATION_ERROR");
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
        Page<PhieuGiamGiaResponse> mapped = phieuGiamGiaList.map(PhieuGiamGiaResponse::new);
        attachUsage(mapped.getContent());
        return mapped;
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

    /**
     * Danh sách mã đang hiệu lực khi checkout: hiện tất cả (kể cả mã cá nhân của khách khác),
     * gắn {@code duocSuDung} để FE khóa chọn nếu khách hiện tại không được phép dùng.
     */
    @Transactional(readOnly = true)
    public Page<PhieuGiamGiaResponse> listAvailableForCustomer(
            String keyword, Integer idKhachHang, Pageable pageable) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        Set<Integer> assignedIds = loadAssignedVoucherIds(idKhachHang);
        return phieuGiamGiaRepository
                .findAvailableForCustomer(normalizedKeyword, pageable)
                .map(v -> {
                    PhieuGiamGiaResponse res = new PhieuGiamGiaResponse(v);
                    res.setDuocSuDung(isDuocSuDung(v, assignedIds));
                    return res;
                });
    }

    private Set<Integer> loadAssignedVoucherIds(Integer idKhachHang) {
        if (idKhachHang == null) {
            return Set.of();
        }
        return khachHangPhieuGiamGiaRepository.findVoucherByKhachHangId(idKhachHang).stream()
                .map(PhieuGiamGia::getId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean isDuocSuDung(PhieuGiamGia voucher, Set<Integer> assignedIds) {
        if (voucher.getPhamVi() == null || voucher.getPhamVi() == PhamViPhieuGiamGia.CONG_KHAI) {
            return true;
        }
        if (voucher.getPhamVi() != PhamViPhieuGiamGia.CA_NHAN) {
            return true;
        }
        return assignedIds.contains(voucher.getId());
    }

    /** Danh sách mã giảm giá khả dụng tại quầy (không FREE_SHIP; mã cá nhân theo khách đã chọn). */
    @Transactional(readOnly = true)
    public Page<PhieuGiamGiaResponse> listAvailableForPos(
            String keyword, Integer idKhachHang, Pageable pageable) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        return phieuGiamGiaRepository
                .findAvailableForPos(normalizedKeyword, idKhachHang, pageable)
                .map(PhieuGiamGiaResponse::new);
    }

//    public Page<PhieuGiamGiaResponse> paginition(return null)

}
