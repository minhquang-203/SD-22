package org.example.templatejava6.product.service;

import org.example.templatejava6.category.service.CategoryService;
import org.example.templatejava6.common.entity.LoaiDa;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.repository.LoaiDaRepository;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.product.entity.*;
import org.example.templatejava6.product.model.request.AnhSanPhamRequest;
import org.example.templatejava6.product.model.request.ChiTietSanPhamRequest;
import org.example.templatejava6.product.model.request.SanPhamRequest;
import org.example.templatejava6.product.model.response.*;
import org.example.templatejava6.product.repository.*;
import org.example.templatejava6.review.repository.DanhGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<SanPhamResponse> getAll() {
        return sanPhamRepository.findByTrangThaiTrue().stream().map(SanPhamResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<SanPhamResponse> phanTrang(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return sanPhamRepository.findByTrangThaiTrue(pageable).map(SanPhamResponse::new);
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponse> timKiem(String keyword) {
        return sanPhamRepository.findByTenContainingIgnoreCaseAndTrangThaiTrue(keyword)
                .stream().map(SanPhamResponse::new).toList();
    }

    public Page<SanPhamResponse> timKiemPhanTrang(String keyword, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return sanPhamRepository.findByTenContainingIgnoreCaseAndTrangThaiTrue(keyword, pageable)
                .map(SanPhamResponse::new);
    }

    @Transactional(readOnly = true)
    public SanPhamDetailResponse detail(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
        return buildDetailResponse(sp);
    }

    @Transactional
    public void add(SanPhamRequest request) {
        validateMaSanPham(request.getMaSanPham(), null);
        validateChiTiets(request.getChiTiets(), null);

        SanPham sp = MapperUtil.map(request, SanPham.class);
        sp.setThuongHieu(categoryService.getThuongHieuOrThrow(request.getIdThuongHieu()));
        sp.setDanhMuc(categoryService.getDanhMucOrThrow(request.getIdDanhMuc()));
        sp.setDangSanPham(categoryService.getDangSanPhamOrThrow(request.getIdDangSanPham()));
        sp.setTrangThai(true);
        sp.setNgayTao(LocalDateTime.now());
        if (sp.getKhangNuoc() == null) {
            sp.setKhangNuoc(false);
        }
        sp = sanPhamRepository.save(sp);

        saveRelations(sp, request);
    }

    @Transactional
    public void update(Integer id, SanPhamRequest request) {
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

        chiTietSanPhamRepository.deleteBySanPham(sp);
        anhSanPhamRepository.deleteBySanPham(sp);
        sanPhamLoaiDaRepository.deleteBySanPham(sp);
        sanPhamCongDungRepository.deleteBySanPham(sp);
        sanPhamThanhPhanRepository.deleteBySanPham(sp);

        saveRelations(sp, request);
    }

    public void delete(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
        sp.setTrangThai(false);
        sanPhamRepository.save(sp);
    }

    public SanPham getSanPhamOrThrow(Integer id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));
    }

    private SanPhamDetailResponse buildDetailResponse(SanPham sp) {
        SanPhamDetailResponse response = new SanPhamDetailResponse(sp);
        response.setChiTiets(chiTietSanPhamRepository.findBySanPhamAndTrangThaiTrue(sp)
                .stream().map(ChiTietSanPhamResponse::new).toList());
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

    private void saveRelations(SanPham sp, SanPhamRequest request) {
        if (request.getChiTiets() != null) {
            for (ChiTietSanPhamRequest ctReq : request.getChiTiets()) {
                ChiTietSanPham ct = new ChiTietSanPham();
                ct.setSanPham(sp);
                ct.setMauSac(categoryService.getMauSacOrNull(ctReq.getIdMauSac()));
                ct.setSku(ctReq.getSku());
                ct.setDungTichMl(ctReq.getDungTichMl());
                ct.setGiaBan(ctReq.getGiaBan());
                ct.setSoLuongTon(ctReq.getSoLuongTon() != null ? ctReq.getSoLuongTon() : 0);
                ct.setHanSuDung(ctReq.getHanSuDung());
                ct.setTrangThai(true);
                chiTietSanPhamRepository.save(ct);
            }
        }
        if (request.getAnhs() != null) {
            for (AnhSanPhamRequest anhReq : request.getAnhs()) {
                AnhSanPham anh = new AnhSanPham();
                anh.setSanPham(sp);
                anh.setUrl(anhReq.getUrl());
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
