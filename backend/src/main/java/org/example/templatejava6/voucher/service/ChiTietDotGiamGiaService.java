package org.example.templatejava6.voucher.service;

import org.example.templatejava6.chat.event.CatalogCacheInvalidateEvent;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.repository.AnhSanPhamRepository;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.voucher.entity.ChiTietDotGiamGia;
import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.example.templatejava6.voucher.model.request.ChiTietDotGiamGiaRequest;
import org.example.templatejava6.voucher.model.response.ChiTietDotGiamGiaResponse;
import org.example.templatejava6.voucher.repository.ChiTietDotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChiTietDotGiamGiaService {

    @Autowired
    private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    @Autowired
    private DotGiamGiaService dotGiamGiaService;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    private AnhSanPhamRepository anhSanPhamRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private void invalidateChatCatalog() {
        eventPublisher.publishEvent(new CatalogCacheInvalidateEvent());
    }

    @Transactional(readOnly = true)
    public List<ChiTietDotGiamGiaResponse> getByDotGiamGia(Integer idDotGiamGia) {
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(idDotGiamGia);
        List<ChiTietDotGiamGia> list = chiTietDotGiamGiaRepository.findByIdDotGiamGia(dgg);
        Map<Integer, String> imageMap = loadPrimaryImages(list);
        return list.stream()
                .map(ct -> {
                    ChiTietDotGiamGiaResponse res = new ChiTietDotGiamGiaResponse(ct);
                    ChiTietSanPham ctsp = ct.getIdChiTietSanPham();
                    if (ctsp != null && ctsp.getSanPham() != null) {
                        res.setAnhUrl(imageMap.get(ctsp.getSanPham().getId()));
                    }
                    return res;
                })
                .toList();
    }

    private Map<Integer, String> loadPrimaryImages(List<ChiTietDotGiamGia> list) {
        Set<Integer> sanPhamIds = list.stream()
                .map(ChiTietDotGiamGia::getIdChiTietSanPham)
                .filter(Objects::nonNull)
                .map(ChiTietSanPham::getSanPham)
                .filter(Objects::nonNull)
                .map(SanPham::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (sanPhamIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<AnhSanPham>> byProduct = anhSanPhamRepository.findBySanPham_IdIn(sanPhamIds).stream()
                .collect(Collectors.groupingBy(a -> a.getSanPham().getId()));
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, List<AnhSanPham>> entry : byProduct.entrySet()) {
            entry.getValue().stream()
                    .sorted(Comparator
                            .comparing((AnhSanPham a) -> !Boolean.TRUE.equals(a.getLaAnhChinh()))
                            .thenComparing(a -> a.getThuTu() != null ? a.getThuTu() : 0))
                    .map(AnhSanPham::getUrl)
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .ifPresent(url -> result.put(entry.getKey(), url));
        }
        return result;
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
        delete(id);
        invalidateChatCatalog();
    }

    @Transactional
    public void add(ChiTietDotGiamGiaRequest request) {
        validateRequiredIds(request);
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(request.getIdDotGiamGia());
        validateDotGiamGiaCoTheThemSanPham(dgg);
        ChiTietSanPham ctsp = getChiTietSanPhamOrThrow(request.getIdChiTietSanPham());
        validateDuplicate(dgg, ctsp.getId(), null);

        ChiTietDotGiamGia ct = new ChiTietDotGiamGia();
        ct.setIdDotGiamGia(dgg);
        ct.setIdChiTietSanPham(ctsp);
        ct.setGiaSauGiam(resolveGiaSauGiam(request, dgg, ctsp));
        chiTietDotGiamGiaRepository.save(ct);
        invalidateChatCatalog();
    }

    @Transactional
    public void update(Integer id, ChiTietDotGiamGiaRequest request) {
        ChiTietDotGiamGia ct = getChiTietOrThrow(id);
        validateRequiredIds(request);
        DotGiamGia dgg = dotGiamGiaService.getDotGiamGiaOrThrow(request.getIdDotGiamGia());
        validateDotGiamGiaCoTheXoaHoacSua(dgg);
        ChiTietSanPham ctsp = getChiTietSanPhamOrThrow(request.getIdChiTietSanPham());
        validateDuplicate(dgg, ctsp.getId(), id);

        ct.setIdDotGiamGia(dgg);
        ct.setIdChiTietSanPham(ctsp);
        ct.setGiaSauGiam(resolveGiaSauGiam(request, dgg, ctsp));
        ct.setId(id);
        chiTietDotGiamGiaRepository.save(ct);
        invalidateChatCatalog();
    }

    @Transactional
    public void delete(Integer id) {
        ChiTietDotGiamGia ct = getChiTietOrThrow(id);
        DotGiamGia dgg = ct.getIdDotGiamGia();
        validateDotGiamGiaCoTheXoaHoacSua(dgg);

        chiTietDotGiamGiaRepository.delete(ct);
        invalidateChatCatalog();
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

    /**
     * Cho phép thêm sản phẩm khi đợt sắp diễn ra hoặc đang chạy.
     * Không cho thêm khi đợt đã kết thúc / ngừng áp dụng.
     */
    private void validateDotGiamGiaCoTheThemSanPham(DotGiamGia dgg) {
        validateDotGiamGiaConHieuLuc(dgg);
        LocalDateTime now = LocalDateTime.now();
        if (dgg.getNgayKetThuc().isBefore(now)) {
            throw new ApiException("Không thể thêm sản phẩm vào đợt giảm giá đã kết thúc", "SALE_IS_OVER");
        }
    }

    /**
     * Chỉ cho phép sửa/xóa sản phẩm khi đợt còn sắp diễn ra.
     * Đợt đang chạy hoặc đã kết thúc không được xóa/sửa sản phẩm.
     */
    private void validateDotGiamGiaCoTheXoaHoacSua(DotGiamGia dgg) {
        validateDotGiamGiaConHieuLuc(dgg);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ngayBatDau = dgg.getNgayBatDau();
        LocalDateTime ngayKetThuc = dgg.getNgayKetThuc();
        if (ngayKetThuc.isBefore(now)) {
            throw new ApiException("Không thể chỉnh sửa đợt giảm giá đã kết thúc", "SALE_IS_OVER");
        }
        if (!ngayBatDau.isAfter(now) && !now.isAfter(ngayKetThuc)) {
            throw new ApiException("Không thể xóa sản phẩm khi đợt giảm giá đang chạy", "SALE_IS_ACTIVE");
        }
    }

    private void validateDotGiamGiaConHieuLuc(DotGiamGia dgg) {
        if (!Boolean.TRUE.equals(dgg.getTrangThai())) {
            throw new ApiException("Đợt giảm giá không tồn tại", "NOT_FOUND");
        }
        if (!Boolean.TRUE.equals(dgg.getIsActive())) {
            throw new ApiException("Đợt giảm giá đã ngừng áp dụng", "VALIDATION_ERROR");
        }
        if (dgg.getNgayBatDau() == null || dgg.getNgayKetThuc() == null) {
            throw new ApiException("Đợt giảm giá chưa được cấu hình thời gian áp dụng", "VALIDATION_ERROR");
        }
    }

    private void validateGiaSauGiam(BigDecimal giaSauGiam, BigDecimal giaBan) {
        if (giaSauGiam == null || giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Giá sau giảm không hợp lệ", "VALIDATION_ERROR");
        }
        if (giaBan != null && giaSauGiam.compareTo(giaBan) >= 0) {
            throw new ApiException("Giá sau giảm phải nhỏ hơn giá bán", "VALIDATION_ERROR");
        }
    }
}
