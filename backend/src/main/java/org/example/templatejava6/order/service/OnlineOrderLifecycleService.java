package org.example.templatejava6.order.service;

import org.example.templatejava6.cart.entity.ChiTietGioHang;
import org.example.templatejava6.cart.entity.GioHang;
import org.example.templatejava6.cart.repository.ChiTietGioHangRepository;
import org.example.templatejava6.cart.repository.GioHangRepository;
import org.example.templatejava6.common.entity.KhachHang;
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
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.realtime.service.OrderRealtimeService;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OnlineOrderLifecycleService {

    private static final String LOAI_DON_ONLINE = "ONLINE";
    private static final String MA_VNPAY = "VNPAY";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final GioHangRepository gioHangRepository;
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final LoHangService loHangService;
    private final RefundService refundService;
    private final OrderRealtimeService orderRealtimeService;

    public OnlineOrderLifecycleService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanHoaDonRepository thanhToanHoaDonRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            GioHangRepository gioHangRepository,
            ChiTietGioHangRepository chiTietGioHangRepository,
            LoHangService loHangService,
            RefundService refundService,
            OrderRealtimeService orderRealtimeService) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.gioHangRepository = gioHangRepository;
        this.chiTietGioHangRepository = chiTietGioHangRepository;
        this.loHangService = loHangService;
        this.refundService = refundService;
        this.orderRealtimeService = orderRealtimeService;
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
        // VNPAY chưa trả: xóa hẳn, không để DA_HUY ảo; hoàn giỏ.
        if (!daThanhToan && laVnpay(hoaDon)) {
            xoaDonChuaThanhToan(hoaDon);
            return true;
        }

        TrangThaiDonHang trangThaiCu = hoaDon.getTrangThai();
        hoanTonKho(hoaDon);
        hoanLuotVoucher(hoaDon);
        hoaDon.setTrangThai(TrangThaiDonHang.DA_HUY);
        hoaDonRepository.save(hoaDon);
        ghiNhatKy(hoaDon, "DA_HUY", ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Hủy đơn online");
        orderRealtimeService.publishStatusChanged(hoaDon, trangThaiCu);

        if (daThanhToan) {
            refundService.taoHoanTienChoXuLy(
                    hoaDon, LoaiHoanTien.HUY_DON, hoaDon.getThanhTien(), null, null, null, null);
        }
        return true;
    }

    /**
     * Xóa đơn VNPAY quá hạn theo id — load lại trong transaction để tránh LazyInitializationException
     * khi scheduler/query trả entity detached.
     */
    @Transactional
    public void xoaDonChuaThanhToanNeuCan(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon).orElse(null);
        if (hoaDon == null) {
            return;
        }
        if (daThanhToanThanhCong(hoaDon)) {
            return;
        }
        xoaDonChuaThanhToan(hoaDon);
    }

    /**
     * Xóa hẳn đơn online VNPAY chưa thanh toán (hủy/thất bại/quá hạn).
     * Hoàn tồn, voucher và trả lại giỏ hàng.
     * Caller phải gọi trong transaction với entity còn gắn session (hoặc dùng {@link #xoaDonChuaThanhToanNeuCan}).
     */
    @Transactional
    public void xoaDonChuaThanhToan(HoaDon hoaDon) {
        if (!LOAI_DON_ONLINE.equalsIgnoreCase(hoaDon.getLoaiDon())) {
            throw new ApiException("Chỉ hỗ trợ xóa đơn online chưa thanh toán.", "INVALID_ORDER_TYPE");
        }
        if (daThanhToanThanhCong(hoaDon)) {
            throw new ApiException("Đơn đã thanh toán, không thể xóa.", "ORDER_ALREADY_PAID");
        }
        if (!laVnpay(hoaDon)) {
            throw new ApiException("Chỉ xóa được đơn VNPAY chưa thanh toán.", "INVALID_PAYMENT_METHOD");
        }

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByIdHoaDon(hoaDon);
        hoanTonKho(hoaDon);
        hoanLuotVoucher(hoaDon);
        hoanGioHang(hoaDon, chiTiets);

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

    @Transactional(readOnly = true)
    public boolean laVnpayChuaThanhToan(HoaDon hoaDon) {
        return laVnpay(hoaDon) && !daThanhToanThanhCong(hoaDon);
    }

    private boolean laVnpay(HoaDon hoaDon) {
        return hoaDon.getIdPhuongThucThanhToan() != null
                && hoaDon.getIdPhuongThucThanhToan().getMa() != null
                && MA_VNPAY.equalsIgnoreCase(hoaDon.getIdPhuongThucThanhToan().getMa());
    }

    private void hoanGioHang(HoaDon hoaDon, List<HoaDonChiTiet> chiTiets) {
        KhachHang khachHang = hoaDon.getIdKhachHang();
        if (khachHang == null || chiTiets.isEmpty()) {
            return;
        }
        GioHang gioHang = gioHangRepository.findFirstByKhachHang_IdOrderByIdAsc(khachHang.getId())
                .orElseGet(() -> {
                    GioHang moi = new GioHang();
                    moi.setKhachHang(khachHang);
                    moi.setNgayTao(LocalDateTime.now());
                    return gioHangRepository.save(moi);
                });

        for (HoaDonChiTiet chiTiet : chiTiets) {
            ChiTietSanPham chiTietSanPham = chiTiet.getIdChiTietSanPham();
            if (chiTietSanPham == null || chiTiet.getSoLuong() == null || chiTiet.getSoLuong() <= 0) {
                continue;
            }
            chiTietGioHangRepository
                    .findByGioHang_IdAndChiTietSanPham_Id(gioHang.getId(), chiTietSanPham.getId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setSoLuong(existing.getSoLuong() + chiTiet.getSoLuong());
                                chiTietGioHangRepository.save(existing);
                            },
                            () -> {
                                ChiTietGioHang item = new ChiTietGioHang();
                                item.setGioHang(gioHang);
                                item.setChiTietSanPham(chiTietSanPham);
                                item.setSoLuong(chiTiet.getSoLuong());
                                chiTietGioHangRepository.save(item);
                            });
        }
    }

    private void hoanTonKho(HoaDon hoaDon) {
        for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
            loHangService.hoanTonTheoChiTiet(chiTiet);
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
