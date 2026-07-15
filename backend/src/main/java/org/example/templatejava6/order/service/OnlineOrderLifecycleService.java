package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiHoanTien;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
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

import java.time.LocalDateTime;

@Service
public class OnlineOrderLifecycleService {

    private static final String LOAI_DON_ONLINE = "ONLINE";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final LoHangService loHangService;
    private final RefundService refundService;

    public OnlineOrderLifecycleService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanHoaDonRepository thanhToanHoaDonRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            LoHangService loHangService,
            RefundService refundService) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.loHangService = loHangService;
        this.refundService = refundService;
    }

    @Transactional
    public boolean huyDonOnline(Integer idHoaDon, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn.", "NOT_FOUND"));
        return huyDonOnline(hoaDon, ghiChu);
    }

    @Transactional
    public boolean huyDonOnline(HoaDon hoaDon, String ghiChu) {
        if (!LOAI_DON_ONLINE.equalsIgnoreCase(hoaDon.getLoaiDon())) {
            throw new ApiException("Chỉ hỗ trợ hủy đơn online.", "INVALID_ORDER_TYPE");
        }
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai().laTrangThaiKetThuc()) {
            return false;
        }
        if (!hoaDon.getTrangThai().coTheHuyTruocKhiGiao()) {
            throw new ApiException(
                    "Đơn hàng đã chuyển sang đang giao hoặc không thể hủy.",
                    "ORDER_CANNOT_CANCEL");
        }

        boolean daThanhToan = daThanhToanThanhCong(hoaDon);
        hoanTonKho(hoaDon);
        hoanLuotVoucher(hoaDon);
        hoaDon.setTrangThai(TrangThaiDonHang.DA_HUY);
        hoaDonRepository.save(hoaDon);
        ghiNhatKy(hoaDon, "DA_HUY", ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Hủy đơn online");

        // Don da thanh toan (VNPAY) ma huy truoc khi giao -> tao yeu cau hoan tien cho admin xu ly.
        if (daThanhToan) {
            refundService.taoHoanTienChoXuLy(
                    hoaDon, LoaiHoanTien.HUY_DON, hoaDon.getThanhTien(), null, null, null, null);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean daThanhToanThanhCong(HoaDon hoaDon) {
        return thanhToanHoaDonRepository
                .findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_THANH_CONG)
                .isPresent();
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
