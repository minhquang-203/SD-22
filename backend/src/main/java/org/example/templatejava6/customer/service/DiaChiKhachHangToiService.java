package org.example.templatejava6.customer.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.entity.DiaChiKhachHang;
import org.example.templatejava6.customer.model.request.DiaChiKhachHangRequest;
import org.example.templatejava6.customer.model.response.DiaChiResponse;
import org.example.templatejava6.customer.repository.DiaChiKhachHangRepository;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiaChiKhachHangToiService {

    @Autowired
    private DiaChiKhachHangRepository diaChiKhachHangRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Transactional(readOnly = true)
    public List<DiaChiResponse> danhSach() {
        return diaChiKhachHangRepository.findByKhachHangOrderByMacDinhDescIdDesc(getKhachDangNhap()).stream()
                .map(DiaChiResponse::new)
                .toList();
    }

    @Transactional
    public DiaChiResponse tao(DiaChiKhachHangRequest request) {
        KhachHang kh = getKhachDangNhap();
        boolean macDinh = Boolean.TRUE.equals(request.getMacDinh())
                || diaChiKhachHangRepository.findByKhachHangOrderByMacDinhDescIdDesc(kh).isEmpty();

        if (macDinh) {
            boMacDinhCuaKhach(kh);
        }

        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setKhachHang(kh);
        applyRequest(dc, request);
        dc.setMacDinh(macDinh);
        return new DiaChiResponse(diaChiKhachHangRepository.save(dc));
    }

    @Transactional
    public DiaChiResponse capNhat(Integer id, DiaChiKhachHangRequest request) {
        KhachHang kh = getKhachDangNhap();
        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHang(id, kh)
                .orElseThrow(() -> new ApiException("Không tìm thấy địa chỉ", "NOT_FOUND"));

        boolean macDinh = Boolean.TRUE.equals(request.getMacDinh());
        if (macDinh) {
            boMacDinhCuaKhach(kh);
        }

        applyRequest(dc, request);
        dc.setMacDinh(macDinh || Boolean.TRUE.equals(dc.getMacDinh()));
        return new DiaChiResponse(diaChiKhachHangRepository.save(dc));
    }

    private void applyRequest(DiaChiKhachHang dc, DiaChiKhachHangRequest request) {
        dc.setHoTenNguoiNhan(request.getHoTenNguoiNhan().trim());
        dc.setSoDienThoai(request.getSoDienThoai().trim());
        dc.setProvinceId(request.getProvinceId());
        dc.setDistrictId(request.getDistrictId());
        dc.setWardCode(request.getWardCode().trim());
        dc.setTinhThanh(request.getTinhThanh().trim());
        dc.setQuanHuyen(request.getQuanHuyen().trim());
        dc.setPhuongXa(request.getPhuongXa().trim());
        dc.setDiaChiChiTiet(request.getDiaChiChiTiet().trim());
    }

    private void boMacDinhCuaKhach(KhachHang kh) {
        diaChiKhachHangRepository.findByKhachHangOrderByMacDinhDescIdDesc(kh).forEach((dc) -> {
            if (Boolean.TRUE.equals(dc.getMacDinh())) {
                dc.setMacDinh(false);
                diaChiKhachHangRepository.save(dc);
            }
        });
    }

    private KhachHang getKhachDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ApiException("Chưa đăng nhập", "UNAUTHORIZED");
        }
        Integer id;
        try {
            id = Integer.parseInt(auth.getName());
        } catch (NumberFormatException ex) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy tài khoản", "NOT_FOUND"));
    }
}
