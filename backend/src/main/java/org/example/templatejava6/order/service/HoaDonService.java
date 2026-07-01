package org.example.templatejava6.order.service;



import org.example.templatejava6.common.entity.KhachHang;

import org.example.templatejava6.common.entity.NhanVien;

import org.example.templatejava6.common.entity.PhieuGiamGia;

import org.example.templatejava6.common.entity.PhuongThucThanhToan;

import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.example.templatejava6.common.enums.TrangThaiDonHang;

import org.example.templatejava6.common.exception.ApiException;

import org.example.templatejava6.order.entity.*;

import org.example.templatejava6.order.model.request.HoaDonChiTietRequest;

import org.example.templatejava6.order.model.request.HoaDonRequest;

import org.example.templatejava6.order.model.response.HoaDonChiTietResponse;

import org.example.templatejava6.order.model.response.HoaDonDetailResponse;

import org.example.templatejava6.order.model.response.HoaDonResponse;

import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.repository.*;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;

import org.example.templatejava6.product.entity.ChiTietSanPham;

import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.math.BigDecimal;

import java.math.RoundingMode;

import java.time.LocalDateTime;

import java.util.List;



@Service

public class HoaDonService {



    @Autowired private HoaDonRepository hoaDonRepository;

    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired private LichSuDonHangRepository lichSuDonHangRepository;

    @Autowired private KhachHangRepository khachHangRepository;

    @Autowired private NhanVienRepository nhanVienRepository;

    @Autowired private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired private ThanhToanHoaDonRepository thanhToanHoaDonRepository;

    @Autowired private GhnOrderCreationService ghnOrderCreationService;



    @Transactional(readOnly = true)

    public List<HoaDonResponse> getAll() {

        return hoaDonRepository.findAllByOrderByNgayTaoDesc().stream().map(HoaDonResponse::new).toList();

    }



    @Transactional(readOnly = true)

    public Page<HoaDonResponse> phanTrang(Integer pageNo, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return hoaDonRepository.findAllByOrderByNgayTaoDesc(pageable).map(HoaDonResponse::new);

    }



    @Transactional(readOnly = true)

    public List<HoaDonResponse> timKiem(String keyword) {

        return hoaDonRepository.findByMaHoaDonContainingIgnoreCase(keyword)

                .stream().map(HoaDonResponse::new).toList();

    }



    @Transactional(readOnly = true)

    public HoaDonDetailResponse detail(Integer id) {

        HoaDon hd = getHoaDonOrThrow(id);

        return buildDetailResponse(hd);

    }



    @Transactional

    public void add(HoaDonRequest request) {

        validateMaHoaDon(request.getMaHoaDon(), null);

        validateChiTiets(request.getChiTiets());



        HoaDon hd = buildHoaDonFromRequest(request);

        hd.setNgayTao(LocalDateTime.now());

        if (hd.getLoaiDon() == null) {

            hd.setLoaiDon("ONLINE");

        }

        hd.setTrangThai(TrangThaiDonHang.CHO_XAC_NHAN);

        if (hd.getPhiVanChuyen() == null) {

            hd.setPhiVanChuyen(BigDecimal.ZERO);

        }

        hd = hoaDonRepository.save(hd);



        saveChiTiets(hd, request.getChiTiets());

        recalculateTotals(hd);

        ghiLichSuTrangThai(hd, TrangThaiDonHang.CHO_XAC_NHAN, "Đơn mới tạo", request.getIdNhanVien());

    }



    @Transactional

    public void update(Integer id, HoaDonRequest request) {

        HoaDon hd = getHoaDonOrThrow(id);

        validateMaHoaDon(request.getMaHoaDon(), id);

        validateChiTiets(request.getChiTiets());



        applyRequestToHoaDon(hd, request);

        hoaDonRepository.save(hd);



        hoaDonChiTietRepository.deleteByIdHoaDon(hd);

        saveChiTiets(hd, request.getChiTiets());

        recalculateTotals(hd);

    }



    @Transactional

    public void chuyenTrangThai(Integer id, TrangThaiDonHang trangThaiMoi, String ghiChu, Integer idNhanVien) {

        HoaDon hd = getHoaDonOrThrow(id);

        TrangThaiDonHang trangThaiCu = hd.getTrangThai();



        if (!trangThaiCu.coTheChuyenSang(trangThaiMoi)) {

            throw new ApiException(

                    "Không thể chuyển từ '" + trangThaiCu.getLabel() + "' sang '" + trangThaiMoi.getLabel() + "'",

                    "VALIDATION_ERROR");

        }



        hd.setTrangThai(trangThaiMoi);

        hoaDonRepository.save(hd);

        ghiLichSuTrangThai(hd, trangThaiMoi, ghiChu, idNhanVien);

        if (canTaoVanDonGhn(trangThaiMoi)) {

            ghnOrderCreationService.taoVanDonNeuCan(hd);

        }

    }

    private boolean canTaoVanDonGhn(TrangThaiDonHang trangThai) {

        return trangThai == TrangThaiDonHang.DA_XAC_NHAN

                || trangThai == TrangThaiDonHang.DANG_CHUAN_BI

                || trangThai == TrangThaiDonHang.DANG_GIAO;

    }



    @Transactional

    public void delete(Integer id) {

        chuyenTrangThai(id, TrangThaiDonHang.DA_HUY, "Hủy đơn hàng", null);

    }



    public HoaDon getHoaDonOrThrow(Integer id) {

        return hoaDonRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "NOT_FOUND"));

    }



    @Transactional

    public void recalculateTotals(HoaDon hd) {

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByIdHoaDon(hd);

        BigDecimal tongTien = chiTiets.stream()

                .map(HoaDonChiTiet::getThanhTien)

                .reduce(BigDecimal.ZERO, BigDecimal::add);



        BigDecimal tienGiamGia = calculateDiscount(tongTien, hd.getIdPhieuGiamGia());

        BigDecimal phiVanChuyen = hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO;

        BigDecimal thanhTien = tongTien.subtract(tienGiamGia).add(phiVanChuyen);



        hd.setTongTien(tongTien);

        hd.setTienGiamGia(tienGiamGia);

        hd.setThanhTien(thanhTien);

        hoaDonRepository.save(hd);

    }



    HoaDonChiTiet buildChiTiet(HoaDon hoaDon, HoaDonChiTietRequest ctReq) {

        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(ctReq.getIdChiTietSanPham())

                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm", "NOT_FOUND"));



        BigDecimal donGia = ctReq.getDonGia() != null ? ctReq.getDonGia() : ctsp.getGiaBan();

        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ApiException("Đơn giá phải lớn hơn 0", "VALIDATION_ERROR");

        }



        HoaDonChiTiet ct = new HoaDonChiTiet();

        ct.setIdHoaDon(hoaDon);

        ct.setIdChiTietSanPham(ctsp);

        ct.setSoLuong(ctReq.getSoLuong());

        ct.setDonGia(donGia);

        ct.setThanhTien(donGia.multiply(BigDecimal.valueOf(ctReq.getSoLuong())));

        return ct;

    }



    private void ghiLichSuTrangThai(HoaDon hoaDon, TrangThaiDonHang trangThai, String ghiChu, Integer idNhanVien) {

        LichSuDonHang ls = new LichSuDonHang();

        ls.setIdHoaDon(hoaDon);

        ls.setTrangThai(trangThai.name());

        ls.setGhiChu(ghiChu);

        ls.setIdNhanVien(resolveNhanVien(idNhanVien));

        ls.setThoiGian(LocalDateTime.now());

        lichSuDonHangRepository.save(ls);

    }



    private HoaDonDetailResponse buildDetailResponse(HoaDon hd) {

        HoaDonDetailResponse response = new HoaDonDetailResponse(hd);

        response.setChiTiets(hoaDonChiTietRepository.findByIdHoaDon(hd)

                .stream().map(HoaDonChiTietResponse::new).toList());

        thanhToanHoaDonRepository.findLatestByHoaDon(hd).ifPresent(tt -> {
            response.setSoTienKhachDua(tt.getSoTienKhachDua());
            response.setTienThua(tt.getTienThua());
            response.setMaGiaoDich(tt.getMaGiaoDich());
        });

        return response;

    }



    private HoaDon buildHoaDonFromRequest(HoaDonRequest request) {

        HoaDon hd = new HoaDon();

        applyRequestToHoaDon(hd, request);

        hd.setMaHoaDon(request.getMaHoaDon());

        return hd;

    }



    private void applyRequestToHoaDon(HoaDon hd, HoaDonRequest request) {

        hd.setMaHoaDon(request.getMaHoaDon());

        hd.setIdKhachHang(resolveKhachHang(request.getIdKhachHang()));

        hd.setIdNhanVien(resolveNhanVien(request.getIdNhanVien()));

        hd.setIdPhuongThucThanhToan(resolvePhuongThucThanhToan(request.getIdPhuongThucThanhToan()));

        hd.setIdPhieuGiamGia(resolvePhieuGiamGia(request.getIdPhieuGiamGia()));

        if (request.getLoaiDon() != null) {

            hd.setLoaiDon(request.getLoaiDon());

        }

        hd.setDiaChiGiao(request.getDiaChiGiao());

        hd.setPhiVanChuyen(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO);

        hd.setMaVanDonGhn(normalizeBlankToNull(request.getMaVanDonGhn()));

        hd.setGhiChu(request.getGhiChu());

    }



    private void saveChiTiets(HoaDon hd, List<HoaDonChiTietRequest> chiTiets) {

        for (HoaDonChiTietRequest ctReq : chiTiets) {

            hoaDonChiTietRepository.save(buildChiTiet(hd, ctReq));

        }

    }



    private KhachHang resolveKhachHang(Integer id) {

        if (id == null) {

            return null;

        }

        return khachHangRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));

    }



    private NhanVien resolveNhanVien(Integer id) {

        if (id == null) {

            return null;

        }

        return nhanVienRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));

    }



    private PhuongThucThanhToan resolvePhuongThucThanhToan(Integer id) {

        if (id == null) {

            return null;

        }

        return phuongThucThanhToanRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy phương thức thanh toán", "NOT_FOUND"));

    }



    private PhieuGiamGia resolvePhieuGiamGia(Integer id) {

        if (id == null) {

            return null;

        }

        return phieuGiamGiaRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));

    }



    private void validateMaHoaDon(String maHoaDon, Integer excludeId) {

        boolean exists = excludeId == null

                ? hoaDonRepository.existsByMaHoaDon(maHoaDon)

                : hoaDonRepository.existsByMaHoaDonAndIdNot(maHoaDon, excludeId);

        if (exists) {

            throw new ApiException("Mã hóa đơn đã tồn tại", "DUPLICATE");

        }

    }



    private void validateChiTiets(List<HoaDonChiTietRequest> chiTiets) {

        if (chiTiets == null || chiTiets.isEmpty()) {

            throw new ApiException("Hóa đơn phải có ít nhất 1 chi tiết", "VALIDATION_ERROR");

        }

    }



    private BigDecimal calculateDiscount(BigDecimal tongTien, PhieuGiamGia phieu) {

        if (phieu == null) {

            return BigDecimal.ZERO;

        }

        BigDecimal discount;

        if (LoaiPhieuGiamGia.PHAN_TRAM == phieu.getLoai()) {

            discount = tongTien.multiply(phieu.getGiaTri())

                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        } else {

            discount = phieu.getGiaTri();

        }

        if (phieu.getGiamToiDa() != null && discount.compareTo(phieu.getGiamToiDa()) > 0) {

            discount = phieu.getGiamToiDa();

        }

        if (discount.compareTo(tongTien) > 0) {

            discount = tongTien;

        }

        return discount;

    }

    private String normalizeBlankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}

