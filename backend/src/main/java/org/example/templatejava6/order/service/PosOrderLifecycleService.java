package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.service.DiemTichLuyService;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.voucher.service.PhieuGiamGiaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PosOrderLifecycleService {

    private static final String LOAI_TAI_QUAY = "TAI_QUAY";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final PhieuGiamGiaService phieuGiamGiaService;
    private final DiemTichLuyService diemTichLuyService;
    private final LoHangService loHangService;

    public PosOrderLifecycleService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanHoaDonRepository thanhToanHoaDonRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            PhieuGiamGiaService phieuGiamGiaService,
            DiemTichLuyService diemTichLuyService,
            LoHangService loHangService) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.phieuGiamGiaService = phieuGiamGiaService;
        this.diemTichLuyService = diemTichLuyService;
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
        diemTichLuyService.congDiemTuDonHoanThanh(hoaDon);
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
        xoaDonChuaThanhToan(hoaDon);
        return true;
    }

    private void xoaDonChuaThanhToan(HoaDon hoaDon) {
        hoanTonKho(hoaDon);
        hoanLuotVoucher(hoaDon);
        thanhToanHoaDonRepository.deleteByIdHoaDon(hoaDon);
        lichSuDonHangRepository.deleteByIdHoaDon_Id(hoaDon.getId());
        hoaDonChiTietRepository.deleteByIdHoaDon(hoaDon);
        hoaDonRepository.delete(hoaDon);
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

    private void hoanTonKho(HoaDon hoaDon) {
        for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
            loHangService.hoanTonTheoChiTiet(chiTiet);
        }
    }

    private void hoanLuotVoucher(HoaDon hoaDon) {
        PhieuGiamGia phieu = hoaDon.getIdPhieuGiamGia();
        if (phieu == null || phieu.getId() == null) {
            return;
        }
        phieuGiamGiaService.restoreOne(phieu.getId());
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
