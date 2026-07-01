package org.example.templatejava6.product.service;

import org.example.templatejava6.category.service.CategoryService;
import org.example.templatejava6.common.entity.LoaiDa;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.repository.LoaiDaRepository;
import org.example.templatejava6.common.service.ProductFileStorageService;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.common.model.response.MaTiepTheoResponse;
import org.example.templatejava6.product.entity.*;
import org.example.templatejava6.product.model.request.AnhSanPhamRequest;
import org.example.templatejava6.product.model.request.ChiTietSanPhamRequest;
import org.example.templatejava6.product.model.request.SanPhamRequest;
import org.example.templatejava6.product.model.response.*;
import org.example.templatejava6.product.repository.*;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository.VariantAgg;
import org.example.templatejava6.review.repository.DanhGiaRepository;
import org.example.templatejava6.voucher.model.response.VariantSaleInfo;
import org.example.templatejava6.voucher.repository.ChiTietDotGiamGiaRepository;
import org.example.templatejava6.voucher.repository.SalePriceAgg;
import org.example.templatejava6.voucher.service.DotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SanPhamService {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private AnhSanPhamRepository anhSanPhamRepository;
    @Autowired private SanPhamLoaiDaRepository sanPhamLoaiDaRepository;
    @Autowired private SanPhamCongDungRepository sanPhamCongDungRepository;
    @Autowired private SanPhamThanhPhanRepository sanPhamThanhPhanRepository;
    @Autowired private LoaiDaRepository loaiDaRepository;
    @Autowired private CategoryService categoryService;
    @Autowired private DanhGiaRepository danhGiaRepository;
    @Autowired private ProductFileStorageService productFileStorageService;
    @Autowired private LoHangService loHangService;
    @Autowired private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    @Autowired private DotGiamGiaService dotGiamGiaService;

    @Transactional(readOnly = true)
    public List<SanPhamResponse> getAll() {
        return getAll(false);
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponse> getAll(Boolean excludeKhuyenMai) {
        Map<Integer, VariantAgg> variantAggMap = loadVariantAggMap();
        Set<Integer> saleProductIds = Boolean.TRUE.equals(excludeKhuyenMai)
                ? loadActiveSaleProductIds()
                : Set.of();
        return sanPhamRepository.findAll().stream()
                .filter(sp -> !saleProductIds.contains(sp.getId()))
                .map(sp -> toListResponse(sp, variantAggMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SanPhamResponse> phanTrang(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Map<Integer, VariantAgg> variantAggMap = loadVariantAggMap();
        return sanPhamRepository.findAll(pageable).map(sp -> toListResponse(sp, variantAggMap));
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponse> timKiem(String keyword) {
        return timKiem(keyword, false);
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponse> timKiem(String keyword, Boolean excludeKhuyenMai) {
        Map<Integer, VariantAgg> variantAggMap = loadVariantAggMap();
        Set<Integer> saleProductIds = Boolean.TRUE.equals(excludeKhuyenMai)
                ? loadActiveSaleProductIds()
                : Set.of();
        return sanPhamRepository.findByTenContainingIgnoreCase(keyword)
                .stream()
                .filter(sp -> !saleProductIds.contains(sp.getId()))
                .map(sp -> toListResponse(sp, variantAggMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponse> getDangKhuyenMai() {
        List<Integer> ids = chiTietDotGiamGiaRepository.findSanPhamIdsInActiveSales();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Integer, VariantAgg> variantAggMap = loadVariantAggMap();
        Map<Integer, SalePriceAgg> salePriceMap = loadActiveSalePriceMap();
        return sanPhamRepository.findAllById(ids).stream()
                .filter(sp -> sp.getTrangThai() != Boolean.FALSE)
                .map(sp -> toSaleListResponse(sp, variantAggMap, salePriceMap.get(sp.getId())))
                .toList();
    }

    public Page<SanPhamResponse> timKiemPhanTrang(String keyword, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Map<Integer, VariantAgg> variantAggMap = loadVariantAggMap();
        return sanPhamRepository.findByTenContainingIgnoreCase(keyword, pageable)
                .map(sp -> toListResponse(sp, variantAggMap));
    }

    @Transactional(readOnly = true)
    public SanPhamDetailResponse detail(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
        return buildDetailResponse(sp);
    }

    @Transactional(readOnly = true)
    public MaTiepTheoResponse previewMaTiepTheo() {
        return new MaTiepTheoResponse(
                MaGenerator.nextCode("SP", sanPhamRepository.findAll().stream().map(SanPham::getMaSanPham).toList()));
    }

    @Transactional
    public void add(SanPhamRequest request, List<MultipartFile> files) {
        validateChiTiets(request.getChiTiets(), null);

        SanPham sp = MapperUtil.map(request, SanPham.class);
        sp.setMaSanPham(MaGenerator.nextCode("SP", sanPhamRepository.findAll().stream().map(SanPham::getMaSanPham).toList()));
        sp.setThuongHieu(categoryService.getThuongHieuOrThrow(request.getIdThuongHieu()));
        sp.setDanhMuc(categoryService.getDanhMucOrThrow(request.getIdDanhMuc()));
        sp.setDangSanPham(categoryService.getDangSanPhamOrThrow(request.getIdDangSanPham()));
        sp.setTrangThai(true);
        sp.setNgayTao(LocalDateTime.now());
        if (sp.getKhangNuoc() == null) {
            sp.setKhangNuoc(false);
        }
        if (sp.getNoiBat() == null) {
            sp.setNoiBat(false);
        }
        sp = sanPhamRepository.save(sp);

        saveRelations(sp, request, files, false);
    }

    @Transactional
    public void update(Integer id, SanPhamRequest request, List<MultipartFile> files) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));

        validateMaSanPham(request.getMaSanPham(), id);
        validateChiTiets(request.getChiTiets(), id);

        MapperUtil.mapToExisting(request, sp);
        sp.setThuongHieu(categoryService.getThuongHieuOrThrow(request.getIdThuongHieu()));
        sp.setDanhMuc(categoryService.getDanhMucOrThrow(request.getIdDanhMuc()));
        sp.setDangSanPham(categoryService.getDangSanPhamOrThrow(request.getIdDangSanPham()));
        sp.setId(id);
        sanPhamRepository.save(sp);

        // Không xóa chi_tiet_san_pham — bảng hoa_don_chi_tiet/gio_hang đang tham chiếu
        anhSanPhamRepository.deleteBySanPham(sp);
        sanPhamLoaiDaRepository.deleteBySanPham(sp);
        sanPhamCongDungRepository.deleteBySanPham(sp);
        sanPhamThanhPhanRepository.deleteBySanPham(sp);
        sanPhamThanhPhanRepository.flush();

        saveRelations(sp, request, files, true);
    }

    public void delete(Integer id) {
        updateTrangThai(id, false);
    }

    @Transactional
    public void updateTrangThai(Integer id, Boolean trangThai) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
        sp.setTrangThai(trangThai);
        sanPhamRepository.save(sp);
    }

    @Transactional
    public void updateNoiBat(Integer id, Boolean noiBat) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
        sp.setNoiBat(noiBat);
        sanPhamRepository.save(sp);
    }

    public SanPham getSanPhamOrThrow(Integer id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
    }

    private SanPhamDetailResponse buildDetailResponse(SanPham sp) {
        Map<Integer, VariantSaleInfo> saleMap = dotGiamGiaService.getActiveSaleByVariantId();
        SanPhamDetailResponse response = new SanPhamDetailResponse(sp);
        response.setChiTiets(chiTietSanPhamRepository.findBySanPham(sp)
                .stream().map(ct -> {
                    ChiTietSanPhamResponse res = new ChiTietSanPhamResponse(ct);
                    res.setHanSuDungGanNhat(loHangService.nearestExpiry(ct.getId()));
                    res.setSapHetHan(loHangService.hasSapHetHan(ct.getId()));
                    applyVariantSale(res, saleMap.get(ct.getId()));
                    return res;
                }).toList());
        applyProductSaleSummary(response, response.getChiTiets());
        response.setAnhs(anhSanPhamRepository.findBySanPhamOrderByThuTuAsc(sp)
                .stream().map(AnhSanPhamResponse::new).toList());
        response.setIdLoaiDas(sanPhamLoaiDaRepository.findBySanPham(sp).stream()
                .map(s -> s.getLoaiDa().getId()).toList());
        response.setIdCongDungs(sanPhamCongDungRepository.findBySanPham(sp).stream()
                .map(s -> s.getCongDung().getId()).toList());
        response.setIdThanhPhans(sanPhamThanhPhanRepository.findBySanPham(sp).stream()
                .map(s -> s.getThanhPhan().getId()).toList());
        response.setDiemTrungBinh(danhGiaRepository.findAverageRatingBySanPham(sp.getId()));
        response.setSoLuongDanhGia(danhGiaRepository.countApprovedBySanPham(sp.getId()));
        return response;
    }

    private Map<Integer, VariantAgg> loadVariantAggMap() {
        Map<Integer, VariantAgg> map = new HashMap<>();
        for (VariantAgg agg : chiTietSanPhamRepository.aggregateActiveVariants()) {
            map.put(agg.getSpId(), agg);
        }
        return map;
    }

    private Map<Integer, SalePriceAgg> loadActiveSalePriceMap() {
        Map<Integer, SalePriceAgg> map = new HashMap<>();
        for (SalePriceAgg agg : chiTietDotGiamGiaRepository.aggregateActiveSalePrices()) {
            map.put(agg.getSanPhamId(), agg);
        }
        return map;
    }

    private Set<Integer> loadActiveSaleProductIds() {
        return new HashSet<>(chiTietDotGiamGiaRepository.findSanPhamIdsInActiveSales());
    }

    private SanPhamResponse toSaleListResponse(
            SanPham sp, Map<Integer, VariantAgg> variantAggMap, SalePriceAgg sale) {
        SanPhamResponse response = toListResponse(sp, variantAggMap);
        if (sale == null) {
            return response;
        }
        response.setGiaGocMin(sale.getGiaGocMin());
        response.setGiaGocMax(sale.getGiaGocMax());
        response.setPhanTramGiam(sale.getPhanTramGiam());

        BigDecimal giaSauGiamMin = sale.getGiaSauGiamMin();
        BigDecimal giaSauGiamMax = sale.getGiaSauGiamMax();
        if (giaSauGiamMin == null && sale.getGiaGocMin() != null && sale.getPhanTramGiam() != null) {
            giaSauGiamMin = dotGiamGiaService.calculateGiaSauGiam(sale.getGiaGocMin(), sale.getPhanTramGiam());
        }
        if (giaSauGiamMax == null && sale.getGiaGocMax() != null && sale.getPhanTramGiam() != null) {
            giaSauGiamMax = dotGiamGiaService.calculateGiaSauGiam(sale.getGiaGocMax(), sale.getPhanTramGiam());
        }
        response.setGiaSauGiamMin(giaSauGiamMin);
        response.setGiaSauGiamMax(giaSauGiamMax);
        return response;
    }

    private void applyVariantSale(ChiTietSanPhamResponse variant, VariantSaleInfo sale) {
        if (variant == null || sale == null) {
            return;
        }
        variant.setGiaGoc(sale.getGiaGoc());
        variant.setGiaSauGiam(sale.getGiaSauGiam());
        variant.setPhanTramGiam(sale.getPhanTramGiam());
    }

    private void applyProductSaleSummary(SanPhamDetailResponse response, List<ChiTietSanPhamResponse> chiTiets) {
        if (chiTiets == null) {
            return;
        }
        BigDecimal giaGocMin = null;
        BigDecimal giaGocMax = null;
        BigDecimal giaSauGiamMin = null;
        BigDecimal giaSauGiamMax = null;
        BigDecimal phanTramGiam = null;
        for (ChiTietSanPhamResponse ct : chiTiets) {
            if (ct.getGiaSauGiam() == null) {
                continue;
            }
            giaGocMin = min(giaGocMin, ct.getGiaGoc());
            giaGocMax = max(giaGocMax, ct.getGiaGoc());
            giaSauGiamMin = min(giaSauGiamMin, ct.getGiaSauGiam());
            giaSauGiamMax = max(giaSauGiamMax, ct.getGiaSauGiam());
            phanTramGiam = max(phanTramGiam, ct.getPhanTramGiam());
        }
        if (giaSauGiamMin == null) {
            return;
        }
        response.setGiaGocMin(giaGocMin);
        response.setGiaGocMax(giaGocMax);
        response.setGiaSauGiamMin(giaSauGiamMin);
        response.setGiaSauGiamMax(giaSauGiamMax);
        response.setPhanTramGiam(phanTramGiam);
    }

    private BigDecimal min(BigDecimal current, BigDecimal value) {
        if (value == null) {
            return current;
        }
        return current == null || value.compareTo(current) < 0 ? value : current;
    }

    private BigDecimal max(BigDecimal current, BigDecimal value) {
        if (value == null) {
            return current;
        }
        return current == null || value.compareTo(current) > 0 ? value : current;
    }

    private SanPhamResponse toListResponse(SanPham sp, Map<Integer, VariantAgg> variantAggMap) {
        SanPhamResponse response = new SanPhamResponse(sp);
        response.setAnhChinhUrl(resolveMainImageUrl(sp.getId()));
        VariantAgg agg = variantAggMap.get(sp.getId());
        if (agg != null) {
            response.setGiaMin(agg.getGiaMin());
            response.setGiaMax(agg.getGiaMax());
            response.setTongTon(agg.getTongTon());
            response.setSoBienThe(agg.getSoBienThe());
        } else {
            response.setGiaMin(null);
            response.setGiaMax(null);
            response.setTongTon(0L);
            response.setSoBienThe(0L);
        }
        return response;
    }

    private String resolveMainImageUrl(Integer sanPhamId) {
        return anhSanPhamRepository.findFirstBySanPham_IdAndLaAnhChinhTrue(sanPhamId)
                .map(AnhSanPham::getUrl)
                .or(() -> anhSanPhamRepository.findFirstBySanPham_IdOrderByThuTuAsc(sanPhamId)
                        .map(AnhSanPham::getUrl))
                .orElse(null);
    }

    private void saveRelations(SanPham sp, SanPhamRequest request, List<MultipartFile> files, boolean isUpdate) {
        saveChiTiets(sp, request.getChiTiets(), isUpdate);
        if (request.getAnhs() != null) {
            for (AnhSanPhamRequest anhReq : request.getAnhs()) {
                String url = resolveImageUrl(anhReq, files);
                if (url == null || url.isBlank()) {
                    continue;
                }
                AnhSanPham anh = new AnhSanPham();
                anh.setSanPham(sp);
                anh.setUrl(url);
                anh.setLaAnhChinh(anhReq.getLaAnhChinh() != null ? anhReq.getLaAnhChinh() : false);
                anh.setThuTu(anhReq.getThuTu() != null ? anhReq.getThuTu() : 0);
                anhSanPhamRepository.save(anh);
            }
        }
        if (request.getIdLoaiDas() != null) {
            for (Integer idLoaiDa : request.getIdLoaiDas()) {
                LoaiDa loaiDa = loaiDaRepository.findById(idLoaiDa)
                        .orElseThrow(() -> new ApiException("Không tìm thấy loại da", "NOT_FOUND"));
                SanPhamLoaiDa spld = new SanPhamLoaiDa();
                spld.setSanPham(sp);
                spld.setLoaiDa(loaiDa);
                sanPhamLoaiDaRepository.save(spld);
            }
        }
        if (request.getIdCongDungs() != null) {
            for (Integer idCongDung : request.getIdCongDungs()) {
                SanPhamCongDung spcd = new SanPhamCongDung();
                spcd.setSanPham(sp);
                spcd.setCongDung(categoryService.getCongDungOrThrow(idCongDung));
                sanPhamCongDungRepository.save(spcd);
            }
        }
        if (request.getIdThanhPhans() != null) {
            for (Integer idThanhPhan : request.getIdThanhPhans()) {
                SanPhamThanhPhan sptp = new SanPhamThanhPhan();
                sptp.setSanPham(sp);
                sptp.setThanhPhan(categoryService.getThanhPhanOrThrow(idThanhPhan));
                sanPhamThanhPhanRepository.save(sptp);
            }
        }
    }

    private void saveChiTiets(SanPham sp, List<ChiTietSanPhamRequest> chiTiets, boolean isUpdate) {
        if (chiTiets == null) {
            return;
        }

        Map<String, ChiTietSanPham> existingBySku = new HashMap<>();
        if (isUpdate) {
            chiTietSanPhamRepository.findBySanPham(sp).forEach(ct -> existingBySku.put(ct.getSku(), ct));
        }

        for (ChiTietSanPhamRequest ctReq : chiTiets) {
            ChiTietSanPham ct = isUpdate ? existingBySku.get(ctReq.getSku()) : null;
            if (ct == null) {
                ct = new ChiTietSanPham();
                ct.setSanPham(sp);
                ct.setSku(ctReq.getSku());
            }
            ct.setMauSac(categoryService.getMauSacOrNull(ctReq.getIdMauSac()));
            ct.setDungTichMl(ctReq.getDungTichMl());
            ct.setGiaBan(ctReq.getGiaBan());
            if (ct.getId() == null) {
                ct.setSoLuongTon(0);
            }
            ct.setTrangThai(true);
            chiTietSanPhamRepository.save(ct);
        }
    }

    private String resolveImageUrl(AnhSanPhamRequest anhReq, List<MultipartFile> files) {
        if (anhReq.getUrl() != null && !anhReq.getUrl().isBlank()) {
            return anhReq.getUrl().trim();
        }
        if (files == null || anhReq.getFileIndex() == null) {
            return null;
        }
        int index = anhReq.getFileIndex();
        if (index < 0 || index >= files.size()) {
            throw new ApiException("File ảnh không khớp với dữ liệu gửi lên", "VALIDATION_ERROR");
        }
        return productFileStorageService.store(files.get(index));
    }

    private void validateMaSanPham(String maSanPham, Integer excludeId) {
        boolean exists = excludeId == null
                ? sanPhamRepository.existsByMaSanPham(maSanPham)
                : sanPhamRepository.existsByMaSanPhamAndIdNot(maSanPham, excludeId);
        if (exists) {
            throw new ApiException("Mã sản phẩm đã tồn tại", "DUPLICATE");
        }
    }

    private void validateChiTiets(List<ChiTietSanPhamRequest> chiTiets, Integer excludeProductId) {
        if (chiTiets == null || chiTiets.isEmpty()) {
            throw new ApiException("Sản phẩm phải có ít nhất 1 biến thể", "VALIDATION_ERROR");
        }
        Set<String> skus = new HashSet<>();
        for (ChiTietSanPhamRequest ct : chiTiets) {
            if (!skus.add(ct.getSku())) {
                throw new ApiException("SKU trùng trong cùng sản phẩm: " + ct.getSku(), "DUPLICATE");
            }
            if (ct.getGiaBan() == null || ct.getGiaBan().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Giá bán phải lớn hơn 0", "VALIDATION_ERROR");
            }
            chiTietSanPhamRepository.findBySku(ct.getSku()).ifPresent(existing -> {
                if (excludeProductId == null
                        || existing.getSanPham() == null
                        || !existing.getSanPham().getId().equals(excludeProductId)) {
                    throw new ApiException("SKU đã tồn tại: " + ct.getSku(), "DUPLICATE");
                }
            });
        }
    }
}
