package org.example.templatejava6.banner.service;

import org.example.templatejava6.banner.entity.BannerTrangChu;
import org.example.templatejava6.banner.model.request.BannerTrangChuRequest;
import org.example.templatejava6.banner.model.response.BannerTrangChuResponse;
import org.example.templatejava6.banner.repository.BannerTrangChuRepository;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.service.ProductFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerTrangChuService {

    @Autowired
    private BannerTrangChuRepository bannerTrangChuRepository;

    @Autowired
    private ProductFileStorageService productFileStorageService;

    public List<BannerTrangChuResponse> listActive() {
        return bannerTrangChuRepository.findByTrangThaiTrueOrderByThuTuAscIdAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BannerTrangChuResponse> listAll() {
        return bannerTrangChuRepository.findAllByOrderByThuTuAscIdAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BannerTrangChuResponse create(BannerTrangChuRequest request) {
        validate(request);
        BannerTrangChu entity = new BannerTrangChu();
        apply(entity, request);
        return toResponse(bannerTrangChuRepository.save(entity));
    }

    @Transactional
    public BannerTrangChuResponse update(Integer id, BannerTrangChuRequest request) {
        validate(request);
        BannerTrangChu entity = bannerTrangChuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy banner", "NOT_FOUND"));
        apply(entity, request);
        return toResponse(bannerTrangChuRepository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        if (!bannerTrangChuRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy banner", "NOT_FOUND");
        }
        bannerTrangChuRepository.deleteById(id);
    }

    public String uploadImage(MultipartFile file) {
        return productFileStorageService.store(file);
    }

    private void validate(BannerTrangChuRequest request) {
        if (request == null) {
            throw new ApiException("Thiếu dữ liệu banner", "VALIDATION_ERROR");
        }
        if (request.getTieuDeChinh() == null || request.getTieuDeChinh().isBlank()) {
            throw new ApiException("Tiêu đề chính không được để trống", "VALIDATION_ERROR");
        }
        if (request.getLinkUrl() == null || request.getLinkUrl().isBlank()) {
            throw new ApiException("Link không được để trống", "VALIDATION_ERROR");
        }
    }

    private void apply(BannerTrangChu entity, BannerTrangChuRequest request) {
        entity.setTieuDe(trimToNull(request.getTieuDe()));
        entity.setTieuDeChinh(request.getTieuDeChinh().trim());
        entity.setMoTa(trimToNull(request.getMoTa()));
        entity.setNutText(trimToNull(request.getNutText()));
        entity.setLinkUrl(request.getLinkUrl().trim());
        entity.setAnhUrl(trimToNull(request.getAnhUrl()));
        entity.setThuTu(request.getThuTu() != null ? request.getThuTu() : 0);
        entity.setTrangThai(request.getTrangThai() == null || Boolean.TRUE.equals(request.getTrangThai()));
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BannerTrangChuResponse toResponse(BannerTrangChu entity) {
        BannerTrangChuResponse res = new BannerTrangChuResponse();
        res.setId(entity.getId());
        res.setTieuDe(entity.getTieuDe());
        res.setTieuDeChinh(entity.getTieuDeChinh());
        res.setMoTa(entity.getMoTa());
        res.setNutText(entity.getNutText());
        res.setLinkUrl(entity.getLinkUrl());
        res.setAnhUrl(entity.getAnhUrl());
        res.setThuTu(entity.getThuTu());
        res.setTrangThai(entity.getTrangThai());
        res.setNgayTao(entity.getNgayTao());
        return res;
    }
}
