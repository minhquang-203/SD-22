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

import org.example.templatejava6.product.service.LoHangService;

import org.example.templatejava6.realtime.service.OrderRealtimeService;

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

    @Autowired private HoanTienRepository hoanTienRepository;

    @Autowired private GhnOrderCreationService ghnOrderCreationService;

    @Autowired private OnlineOrderLifecycleService onlineOrderLifecycleService;

    @Autowired private PosOrderLifecycleService posOrderLifecycleService;

    @Autowired private OrderRealtimeService orderRealtimeService;

    @Autowired private LoHangService loHangService;



    @Transactional(readOnly = true)

    public List<HoaDonResponse> getAll() {

        return hoaDonRepository.findVisibleForAdminOrderByNgayTaoDesc()
                .stream().map(HoaDonResponse::new).toList();

    }



    @Transactional(readOnly = true)

    public Page<HoaDonResponse> phanTrang(Integer pageNo, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return hoaDonRepository.findVisibleForAdminOrderByNgayTaoDesc(pageable)
                .map(HoaDonResponse::new);

    }



    @Transactional(readOnly = true)

    public List<HoaDonResponse> timKiem(String keyword) {

        return hoaDonRepository.findVisibleForAdminByMaHoaDonContaining(keyword)

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

        validateVnpayDaThanhToanNeuCan(hd, trangThaiMoi);



        // Hủy đơn: ưu tiên lifecycle (hoàn tồn + voucher/refund), tránh mất tồn kho.

        if (trangThaiMoi == TrangThaiDonHang.DA_HUY) {

            String note = ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Hủy đơn hàng";

            if ("ONLINE".equalsIgnoreCase(hd.getLoaiDon()) && trangThaiCu.coTheHuyTruocKhiGiao()) {

                onlineOrderLifecycleService.huyDonOnline(hd, note);

                return;

            }

            if ("TAI_QUAY".equalsIgnoreCase(hd.getLoaiDon()) && trangThaiCu == TrangThaiDonHang.CHO_XAC_NHAN) {

                posOrderLifecycleService.huyDonVnpay(hd, note);

                return;

            }

            if (canHoanTonKhiHuy(trangThaiCu, hd.getLoaiDon())) {

                hoanTonTheoHoaDon(hd);

            }

        }



        hd.setTrangThai(trangThaiMoi);

        hoaDonRepository.save(hd);

        ghiLichSuTrangThai(hd, trangThaiMoi, ghiChu, idNhanVien);

        if (canTaoVanDonGhn(trangThaiMoi)) {

            ghnOrderCreationService.taoVanDonNeuCan(hd);

        }

        orderRealtimeService.publishStatusChanged(hd, trangThaiCu);

    }

    /** Đơn đã trừ tồn lúc tạo/giữ thì khi hủy phải hoàn lại. */

    private boolean canHoanTonKhiHuy(TrangThaiDonHang trangThaiCu, String loaiDon) {

        if (trangThaiCu == TrangThaiDonHang.CHO && "TAI_QUAY".equalsIgnoreCase(loaiDon)) {

            return true;

        }

        // Online / tại quầy đã checkout: tồn đã trừ từ lúc tạo đơn.

        return trangThaiCu == TrangThaiDonHang.CHO_XAC_NHAN

                || trangThaiCu == TrangThaiDonHang.DA_XAC_NHAN

                || trangThaiCu == TrangThaiDonHang.DANG_CHUAN_BI

                || trangThaiCu == TrangThaiDonHang.DANG_GIAO;

    }

    private void hoanTonTheoHoaDon(HoaDon hoaDon) {

        for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {

            loHangService.hoanTonTheoChiTiet(chiTiet);

        }

    }

    private boolean canTaoVanDonGhn(TrangThaiDonHang trangThai) {

        return trangThai == TrangThaiDonHang.DA_XAC_NHAN

                || trangThai == TrangThaiDonHang.DANG_CHUAN_BI

                || trangThai == TrangThaiDonHang.DANG_GIAO;

    }



    @Transactional

    public void tuChoiDon(Integer id, String ghiChu, Integer idNhanVien) {

        HoaDon hd = getHoaDonOrThrow(id);

        String note = ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Admin từ chối đơn hàng";

        if ("ONLINE".equalsIgnoreCase(hd.getLoaiDon())) {

            if (!hd.getTrangThai().coTheHuyTruocKhiGiao()) {

                throw new ApiException(

                        "Chỉ hủy được đơn online chưa chuyển sang đang giao.",

                        "VALIDATION_ERROR");

            }

            onlineOrderLifecycleService.huyDonOnline(hd, note);

            return;

        }

        if (hd.getTrangThai() != TrangThaiDonHang.CHO_XAC_NHAN) {

            throw new ApiException("Chỉ từ chối được đơn đang chờ xác nhận.", "VALIDATION_ERROR");

        }

        if ("TAI_QUAY".equalsIgnoreCase(hd.getLoaiDon())) {

            posOrderLifecycleService.huyDonVnpay(hd, note);

            return;

        }

        chuyenTrangThai(id, TrangThaiDonHang.DA_HUY, note, idNhanVien);

    }



    @Transactional

    public void delete(Integer id) {

        chuyenTrangThai(id, TrangThaiDonHang.DA_HUY, "Hủy đơn hàng", null);

    }



    public HoaDon getHoaDonOrThrow(Integer id) {

        HoaDon hd = hoaDonRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "NOT_FOUND"));

        if (onlineOrderLifecycleService.laVnpayChuaThanhToan(hd)) {

            throw new ApiException("Không tìm thấy hóa đơn", "NOT_FOUND");

        }

        return hd;

    }

    private void validateVnpayDaThanhToanNeuCan(HoaDon hd, TrangThaiDonHang trangThaiMoi) {

        if (trangThaiMoi == TrangThaiDonHang.DA_HUY) {

            return;

        }

        PhuongThucThanhToan phuongThuc = hd.getIdPhuongThucThanhToan();

        if (phuongThuc == null || phuongThuc.getMa() == null

                || !"VNPAY".equalsIgnoreCase(phuongThuc.getMa())) {

            return;

        }

        if (!onlineOrderLifecycleService.daThanhToanThanhCong(hd)) {

            throw new ApiException("Đơn VNPAY chưa thanh toán thành công.", "PAYMENT_REQUIRED");

        }

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

        List<HoanTien> hoanTiens = hoanTienRepository.findByIdHoaDonOrderByNgayTaoDesc(hd);
        if (!hoanTiens.isEmpty()) {
            HoanTien ht = hoanTiens.get(0);
            if (ht.getTrangThai() != null) {
                response.setTrangThaiHoanTien(ht.getTrangThai().name());
                response.setTrangThaiHoanTienLabel(ht.getTrangThai().getLabel());
            }
            response.setMaGiaoDichHoan(ht.getMaGiaoDichHoan());
            response.setSoTienHoan(ht.getSoTien());
        }

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

