package org.example.templatejava6.review.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.service.SanPhamService;
import org.example.templatejava6.review.entity.DanhGia;
import org.example.templatejava6.review.model.request.DanhGiaRequest;
import org.example.templatejava6.review.model.response.DanhGiaResponse;
import org.example.templatejava6.review.repository.DanhGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DanhGiaService {

    @Autowired private DanhGiaRepository danhGiaRepository;
    @Autowired private SanPhamService sanPhamService;

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getApprovedBySanPham(Integer idSanPham) {
        SanPham sp = sanPhamService.getSanPhamOrThrow(idSanPham);
        return danhGiaRepository.findBySanPhamAndTrangThai(sp, "DA_DUYET")
                .stream().map(DanhGiaResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getChoDuyet() {
        return danhGiaRepository.findByTrangThai("CHO_DUYET")
                .stream().map(DanhGiaResponse::new).toList();
    }

    @Transactional
    public void add(DanhGiaRequest request) {
        DanhGia dg = MapperUtil.map(request, DanhGia.class);
        dg.setSanPham(sanPhamService.getSanPhamOrThrow(request.getIdSanPham()));
        dg.setTrangThai("CHO_DUYET");
        dg.setNgayTao(LocalDateTime.now());
        danhGiaRepository.save(dg);
    }

    @Transactional
    public void duyet(Integer id, String trangThai) {
        if (!"DA_DUYET".equals(trangThai) && !"TU_CHOI".equals(trangThai)) {
            throw new ApiException("Trạng thái duyệt chỉ được DA_DUYET hoặc TU_CHOI", "VALIDATION_ERROR");
        }
        DanhGia dg = danhGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá", "NOT_FOUND"));
        dg.setTrangThai(trangThai);
        danhGiaRepository.save(dg);
    }
}
