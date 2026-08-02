package org.example.templatejava6.nhaphang.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.nhaphang.entity.ChiTietPhieuNhap;
import org.example.templatejava6.nhaphang.entity.NhaCungCap;
import org.example.templatejava6.nhaphang.entity.PhieuNhap;
import org.example.templatejava6.nhaphang.model.request.PhieuNhapRequest;
import org.example.templatejava6.nhaphang.model.response.BienTheNhapHangResponse;
import org.example.templatejava6.nhaphang.model.response.PhieuNhapResponse;
import org.example.templatejava6.nhaphang.repository.PhieuNhapRepository;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.model.request.LoHangRequest;
import org.example.templatejava6.product.model.response.LoHangResponse;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.product.repository.LoHangRepository;
import org.example.templatejava6.product.service.LoHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PhieuNhapService {

    private static final String PHIEU_TAM = "PHIEU_TAM";
    private static final String DA_NHAP = "DA_NHAP";
    private static final String DA_HUY = "DA_HUY";

    @Autowired private PhieuNhapRepository phieuNhapRepository;
    @Autowired private NhaCungCapService nhaCungCapService;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private LoHangService loHangService;
    @Autowired private LoHangRepository loHangRepository;

    @Transactional(readOnly = true)
    public List<PhieuNhapResponse> list(String trangThai, Integer idNcc, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;
        return phieuNhapRepository.search(trangThai, idNcc, fromDt, toDt).stream()
                .map(PhieuNhapResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PhieuNhapResponse detail(Integer id) {
        PhieuNhap p = phieuNhapRepository.findDetailById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu nhập", "NOT_FOUND"));
        return new PhieuNhapResponse(p);
    }

    @Transactional
    public PhieuNhapResponse luuTam(PhieuNhapRequest request) {
        PhieuNhap p = new PhieuNhap();
        p.setMaPhieuNhap(nextMaPhieu());
        p.setNhanVien(currentNhanVien());
        p.setTrangThai(PHIEU_TAM);
        applyHeaderAndLines(p, request);
        return new PhieuNhapResponse(phieuNhapRepository.save(p));
    }

    @Transactional
    public PhieuNhapResponse updateTam(Integer id, PhieuNhapRequest request) {
        PhieuNhap p = getOrThrow(id);
        ensureTam(p);
        applyHeaderAndLines(p, request);
        return new PhieuNhapResponse(phieuNhapRepository.save(p));
    }

    @Transactional
    public PhieuNhapResponse hoanThanh(Integer id) {
        PhieuNhap p = phieuNhapRepository.findDetailById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu nhập", "NOT_FOUND"));
        ensureTam(p);
        if (p.getChiTiets() == null || p.getChiTiets().isEmpty()) {
            throw new ApiException("Phiếu nhập chưa có dòng hàng", "VALIDATION_ERROR");
        }

        LocalDate ngayNhap = p.getNgayTao() != null ? p.getNgayTao().toLocalDate() : LocalDate.now();
        if (ngayNhap.isAfter(LocalDate.now())) {
            throw new ApiException("Ngày nhập không được lớn hơn ngày hiện tại", "VALIDATION_ERROR");
        }
        int stt = 1;
        for (ChiTietPhieuNhap dong : p.getChiTiets()) {
            if (dong.getHanSuDung() == null) {
                throw new ApiException(
                        "Dòng SKU " + dong.getChiTietSanPham().getSku() + " thiếu hạn sử dụng",
                        "VALIDATION_ERROR");
            }
            String soLo = dong.getSoLo();
            if (soLo == null || soLo.isBlank()) {
                soLo = p.getMaPhieuNhap() + "-" + String.format("%02d", stt);
                dong.setSoLo(soLo);
            }

            LoHangRequest loReq = new LoHangRequest();
            loReq.setIdChiTietSanPham(dong.getChiTietSanPham().getId());
            loReq.setSoLo(soLo);
            loReq.setNgayNhap(ngayNhap);
            loReq.setHanSuDung(dong.getHanSuDung());
            loReq.setSoLuongNhap(dong.getSoLuong());
            loReq.setGhiChu("Nhập từ phiếu " + p.getMaPhieuNhap());

            LoHangResponse created = loHangService.nhapLo(loReq);
            dong.setLoHang(loHangRepository.findById(created.getId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy lô vừa tạo", "NOT_FOUND")));
            stt++;
        }

        p.setTrangThai(DA_NHAP);
        return new PhieuNhapResponse(phieuNhapRepository.save(p));
    }

    @Transactional
    public PhieuNhapResponse huy(Integer id) {
        PhieuNhap p = getOrThrow(id);
        if (DA_NHAP.equals(p.getTrangThai())) {
            throw new ApiException("Phiếu đã nhập kho, không thể hủy (tránh sai tồn)", "INVALID_STATE");
        }
        if (DA_HUY.equals(p.getTrangThai())) {
            return detail(id);
        }
        ensureTam(p);
        p.setTrangThai(DA_HUY);
        phieuNhapRepository.save(p);
        return detail(id);
    }

    private void applyHeaderAndLines(PhieuNhap p, PhieuNhapRequest request) {
        LocalDate ngayNhap = request.getNgayNhap() != null ? request.getNgayNhap() : LocalDate.now();
        if (ngayNhap.isAfter(LocalDate.now())) {
            throw new ApiException("Ngày nhập không được lớn hơn ngày hiện tại", "VALIDATION_ERROR");
        }
        LocalTime timePart = p.getNgayTao() != null ? p.getNgayTao().toLocalTime() : LocalTime.now();
        p.setNgayTao(LocalDateTime.of(ngayNhap, timePart));

        if (request.getIdNhaCungCap() != null) {
            NhaCungCap ncc = nhaCungCapService.getOrThrow(request.getIdNhaCungCap());
            if (!Boolean.TRUE.equals(ncc.getTrangThai())) {
                throw new ApiException("Nhà cung cấp đã ngừng dùng", "INACTIVE");
            }
            p.setNhaCungCap(ncc);
        } else {
            p.setNhaCungCap(null);
        }
        p.setSoHoaDonDauVao(blankToNull(request.getSoHoaDonDauVao()));
        p.setGhiChu(blankToNull(request.getGhiChu()));
        BigDecimal giamGia = request.getGiamGia() != null ? request.getGiamGia() : BigDecimal.ZERO;
        if (giamGia.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Giảm giá không hợp lệ", "VALIDATION_ERROR");
        }
        p.setGiamGia(giamGia);

        p.getChiTiets().clear();
        BigDecimal tong = BigDecimal.ZERO;
        Set<Integer> seen = new HashSet<>();

        if (request.getChiTiets() != null) {
            for (PhieuNhapRequest.DongPhieuNhapRequest dongReq : request.getChiTiets()) {
                if (dongReq.getIdChiTietSanPham() == null) {
                    throw new ApiException("Thiếu biến thể sản phẩm trên dòng", "VALIDATION_ERROR");
                }
                if (!seen.add(dongReq.getIdChiTietSanPham())) {
                    throw new ApiException("Không được trùng SKU trên cùng phiếu", "VALIDATION_ERROR");
                }
                if (dongReq.getSoLuong() == null || dongReq.getSoLuong() <= 0) {
                    throw new ApiException("Số lượng phải lớn hơn 0", "VALIDATION_ERROR");
                }
                BigDecimal donGia = dongReq.getDonGia() != null ? dongReq.getDonGia() : BigDecimal.ZERO;
                if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ApiException("Đơn giá không hợp lệ", "VALIDATION_ERROR");
                }

                ChiTietSanPham ct = chiTietSanPhamRepository.findById(dongReq.getIdChiTietSanPham())
                        .orElseThrow(() -> new ApiException("Không tìm thấy biến thể", "NOT_FOUND"));
                if (!Boolean.TRUE.equals(ct.getTrangThai())) {
                    throw new ApiException("SKU " + ct.getSku() + " không còn hoạt động", "INACTIVE_SKU");
                }

                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(dongReq.getSoLuong()));
                ChiTietPhieuNhap dong = new ChiTietPhieuNhap();
                dong.setPhieuNhap(p);
                dong.setChiTietSanPham(ct);
                dong.setSoLuong(dongReq.getSoLuong());
                dong.setDonGia(donGia);
                dong.setHanSuDung(dongReq.getHanSuDung());
                dong.setSoLo(blankToNull(dongReq.getSoLo()));
                dong.setThanhTien(thanhTien);
                p.getChiTiets().add(dong);
                tong = tong.add(thanhTien);
            }
        }

        p.setTongTien(tong);
        BigDecimal canTra = tong.subtract(giamGia);
        if (canTra.compareTo(BigDecimal.ZERO) < 0) {
            canTra = BigDecimal.ZERO;
        }
        p.setCanTraNcc(canTra);
    }

    @Transactional(readOnly = true)
    public List<BienTheNhapHangResponse> timBienThe(String keyword, int page, int size) {
        String q = keyword == null ? "" : keyword.trim();
        int pageNo = Math.max(0, page);
        int pageSize = size <= 0 ? 20 : Math.min(size, 50);
        return chiTietSanPhamRepository
                .danhSachBienTheBan(q, PageRequest.of(pageNo, pageSize))
                .stream()
                .map(BienTheNhapHangResponse::new)
                .toList();
    }

    private PhieuNhap getOrThrow(Integer id) {
        return phieuNhapRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu nhập", "NOT_FOUND"));
    }

    private void ensureTam(PhieuNhap p) {
        if (!PHIEU_TAM.equals(p.getTrangThai())) {
            throw new ApiException("Chỉ thao tác được trên phiếu tạm", "INVALID_STATE");
        }
    }

    private NhanVien currentNhanVien() {
        Integer id = SecurityUtils.currentNhanVienId();
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Nhân viên không tồn tại", "NOT_FOUND"));
    }

    private String nextMaPhieu() {
        return MaGenerator.nextCode("PN", phieuNhapRepository.findAllMa(), 6);
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }
}
