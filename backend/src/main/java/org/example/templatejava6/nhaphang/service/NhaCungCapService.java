package org.example.templatejava6.nhaphang.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.nhaphang.entity.NhaCungCap;
import org.example.templatejava6.nhaphang.model.request.NhaCungCapRequest;
import org.example.templatejava6.nhaphang.model.response.NhaCungCapResponse;
import org.example.templatejava6.nhaphang.repository.NhaCungCapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NhaCungCapService {

    @Autowired private NhaCungCapRepository nhaCungCapRepository;

    @Transactional(readOnly = true)
    public List<NhaCungCapResponse> list(String keyword, boolean activeOnly) {
        String q = keyword == null ? "" : keyword.trim();
        List<NhaCungCap> rows = activeOnly
                ? nhaCungCapRepository.searchActive(q)
                : nhaCungCapRepository.searchAll(q);
        return rows.stream().map(NhaCungCapResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public NhaCungCapResponse detail(Integer id) {
        return new NhaCungCapResponse(getOrThrow(id));
    }

    @Transactional
    public NhaCungCapResponse create(NhaCungCapRequest request) {
        NhaCungCap n = new NhaCungCap();
        apply(n, request);
        n.setMa(nextMa());
        n.setTrangThai(true);
        return new NhaCungCapResponse(nhaCungCapRepository.save(n));
    }

    @Transactional
    public NhaCungCapResponse update(Integer id, NhaCungCapRequest request) {
        NhaCungCap n = getOrThrow(id);
        apply(n, request);
        return new NhaCungCapResponse(nhaCungCapRepository.save(n));
    }

    @Transactional
    public void softDelete(Integer id) {
        NhaCungCap n = getOrThrow(id);
        n.setTrangThai(false);
        nhaCungCapRepository.save(n);
    }

    public NhaCungCap getOrThrow(Integer id) {
        return nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhà cung cấp", "NOT_FOUND"));
    }

    private void apply(NhaCungCap n, NhaCungCapRequest request) {
        if (request.getTen() == null || request.getTen().isBlank()) {
            throw new ApiException("Tên nhà cung cấp không được để trống", "VALIDATION_ERROR");
        }
        n.setTen(request.getTen().trim());
        n.setSoDienThoai(blankToNull(request.getSoDienThoai()));
        n.setEmail(blankToNull(request.getEmail()));
        n.setDiaChi(blankToNull(request.getDiaChi()));
        n.setGhiChu(blankToNull(request.getGhiChu()));
    }

    private String nextMa() {
        List<String> existing = nhaCungCapRepository.findAll().stream().map(NhaCungCap::getMa).toList();
        return MaGenerator.nextCode("NCC", existing, 4);
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }
}
