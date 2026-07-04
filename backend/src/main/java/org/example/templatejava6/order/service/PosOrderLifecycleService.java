package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PosOrderLifecycleService {

    private static final String LOAI_TAI_QUAY = "TAI_QUAY";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final KhachHangRepository khachHangRepository;
    private final LoHangService loHangService;

    public PosOrderLifecycleService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanHoaDonRepository thanhToanHoaDonRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            KhachHangRepository khachHangRepository,
            LoHangService loHangService) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.khachHangRepository = khachHangRepository;
        this.loHangService = loHangService;
    }

    @Transactional
    public void hoanThanhDonVnpay(HoaDon hoaDon) {
        validateTaiQuay(hoaDon);
        if (hoaDon.getTrangThai() == TrangThaiDonHang.HOAN_THANH) {
            return;
        }
        hoaDon.setTrangThai(TrangThaiDonHang.HOAN_THANH);
        hoaDonRepository.save(hoaDon);
        congDiemTichLuy(hoaDon);
        ghiNhatKy(hoaDon, "HOAN_THANH", "Thanh toán VNPAY thành công tại quầy");
    }

    @Transactional
    public boolean huyDonVnpay(HoaDon hoaDon, String ghiChu) {
        validateTaiQuay(hoaDon);
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai().laTrangThaiKetThuc()) {
            return false;
        }
        if (daThanhToanThanhCong(hoaDon)) {
            throw new ApiException("Đơn đã thanh toán, không thể hủy.", "ORDER_ALREADY_PAID");
        }
        hoanTonKho(hoaDon);
        hoanLuotVoucher(hoaDon);
        hoaDon.setTrangThai(TrangThaiDonHang.DA_HUY);
        hoaDonRepository.save(hoaDon);
        ghiNhatKy(hoaDon, "DA_HUY", ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Hủy thanh toán VNPAY tại quầy");
        return true;
    }

    @Transactional(readOnly = true)
    public boolean daThanhToanThanhCong(HoaDon hoaDon) {
        return thanhToanHoaDonRepository
                .findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_THANH_CONG)
                .isPresent();
    }

    private void validateTaiQuay(HoaDon hoaDon) {
        if (!LOAI_TAI_QUAY.equalsIgnoreCase(hoaDon.getLoaiDon())) {
            throw new ApiException("Chỉ hỗ trợ đơn tại quầy.", "INVALID_ORDER_TYPE");
        }
    }

    private void congDiemTichLuy(HoaDon hoaDon) {
        KhachHang khachHang = hoaDon.getIdKhachHang();
        if (khachHang == null || hoaDon.getThanhTien() == null) {
            return;
        }
        int diemThem = hoaDon.getThanhTien()
                .divide(BigDecimal.valueOf(1000), 0, RoundingMode.FLOOR)
                .intValue();
        int diemHien = khachHang.getDiemTichLuy() != null ? khachHang.getDiemTichLuy() : 0;
        khachHang.setDiemTichLuy(diemHien + diemThem);
        khachHangRepository.save(khachHang);
    }

    private void hoanTonKho(HoaDon hoaDon) {
        for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
            loHangService.hoanTon(chiTiet.getIdChiTietSanPham().getId(), chiTiet.getSoLuong());
        }
    }

    private void hoanLuotVoucher(HoaDon hoaDon) {
        PhieuGiamGia phieu = hoaDon.getIdPhieuGiamGia();
        if (phieu == null) {
            return;
        }
        int soLuong = phieu.getSoLuong() != null ? phieu.getSoLuong() : 0;
        phieu.setSoLuong(soLuong + 1);
        phieuGiamGiaRepository.save(phieu);
    }

    private void ghiNhatKy(HoaDon hoaDon, String trangThai, String ghiChu) {
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setIdHoaDon(hoaDon);
        lichSu.setTrangThai(trangThai);
        lichSu.setGhiChu(ghiChu);
        lichSu.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(lichSu);
    }
}
