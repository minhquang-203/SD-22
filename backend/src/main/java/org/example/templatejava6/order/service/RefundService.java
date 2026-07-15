package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.enums.LoaiHoanTien;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.enums.TrangThaiHoanTien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.OrderMailService;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoanTien;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.model.request.HoanTatHoanTienRequest;
import org.example.templatejava6.order.model.response.HoanTienResponse;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.HoanTienRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.payment.gateway.RefundCommand;
import org.example.templatejava6.payment.gateway.RefundGateway;
import org.example.templatejava6.payment.gateway.RefundGatewayRegistry;
import org.example.templatejava6.payment.gateway.RefundResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quan ly hoan tien: tao ban ghi CHO_XU_LY khi huy don / tra hang;
 * khi admin xac nhan — neu co RefundGateway (VNPAY) thi goi API tu dong,
 * nguoc lai (CHUYEN_KHOAN) thi admin nhap ma giao dich thu cong.
 * Khi hoan thanh cong: ban ghi hoan_tien = DA_HOAN va hoa don = TRA_HANG.
 */
@Service
public class RefundService {

    private static final String MA_VNPAY = "VNPAY";
    private static final String PHUONG_THUC_VNPAY = "VNPAY";
    private static final String PHUONG_THUC_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";

    private final HoanTienRepository hoanTienRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final RefundGatewayRegistry refundGatewayRegistry;
    private final ThongBaoService thongBaoService;
    private final OrderMailService orderMailService;

    public RefundService(HoanTienRepository hoanTienRepository,
                         HoaDonRepository hoaDonRepository,
                         ThanhToanHoaDonRepository thanhToanHoaDonRepository,
                         LichSuDonHangRepository lichSuDonHangRepository,
                         NhanVienRepository nhanVienRepository,
                         RefundGatewayRegistry refundGatewayRegistry,
                         ThongBaoService thongBaoService,
                         OrderMailService orderMailService) {
        this.hoanTienRepository = hoanTienRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.refundGatewayRegistry = refundGatewayRegistry;
        this.thongBaoService = thongBaoService;
        this.orderMailService = orderMailService;
    }

    /** Tao mot ban ghi hoan tien CHO_XU_LY cho admin xu ly. Bao admin qua chuong thong bao. */
    @Transactional
    public HoanTien taoHoanTienChoXuLy(HoaDon hoaDon, LoaiHoanTien loai, BigDecimal soTien,
                                       YeuCauTraHang yeuCau,
                                       String tenNganHang, String soTaiKhoan, String chuTaiKhoan) {
        if (hoaDon == null) {
            throw new ApiException("Không tìm thấy hóa đơn để hoàn tiền.", "NOT_FOUND");
        }
        if (hoanTienRepository.existsByIdHoaDon_IdAndTrangThaiNot(hoaDon.getId(), TrangThaiHoanTien.TU_CHOI)) {
            throw new ApiException(
                    "Đơn hàng đã có yêu cầu hoàn tiền đang chờ hoặc đã hoàn.",
                    "REFUND_ALREADY_EXISTS");
        }

        HoanTien ht = new HoanTien();
        ht.setIdHoaDon(hoaDon);
        ht.setIdYeuCauTraHang(yeuCau);
        ht.setLoai(loai);
        ht.setSoTien(soTien != null ? soTien : BigDecimal.ZERO);
        ht.setPhuongThuc(resolvePhuongThuc(hoaDon));
        ht.setTrangThai(TrangThaiHoanTien.CHO_XU_LY);
        ht.setTenNganHang(tenNganHang);
        ht.setSoTaiKhoan(soTaiKhoan);
        ht.setChuTaiKhoan(chuTaiKhoan);
        ht.setNgayTao(LocalDateTime.now());
        HoanTien saved = hoanTienRepository.save(ht);

        ghiNhatKy(hoaDon, "HOAN_TIEN_CHO_XU_LY",
                "Tạo yêu cầu hoàn tiền (" + (loai != null ? loai.name() : "") + ") số tiền "
                        + dinhDangTien(saved.getSoTien()));

        thongBaoService.taoThongBao(
                LoaiThongBao.YEU_CAU_HOAN_TIEN,
                "Yêu cầu hoàn tiền",
                "Đơn " + hoaDon.getMaHoaDon() + " cần hoàn tiền "
                        + dinhDangTien(saved.getSoTien()) + " ("
                        + (loai != null ? loai.getLabel() : "") + ").",
                "/admin/hoan-tien",
                saved.getId(),
                hoaDon.getMaHoaDon());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<HoanTienResponse> danhSach(TrangThaiHoanTien trangThai) {
        List<HoanTien> list = trangThai != null
                ? hoanTienRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai)
                : hoanTienRepository.findAllByOrderByNgayTaoDesc();
        return list.stream().map(HoanTienResponse::new).toList();
    }

    /**
     * Tao ban ghi hoan tien va goi cong refund ngay (dung cho VNPAY khi duyet tra hang).
     * Thanh cong: hoan_tien = DA_HOAN, hoa_don = TRA_HANG, co maGiaoDichHoan.
     */
    @Transactional
    public HoanTienResponse taoVaHoanTuDong(HoaDon hoaDon, LoaiHoanTien loai, BigDecimal soTien,
                                            YeuCauTraHang yeuCau,
                                            String tenNganHang, String soTaiKhoan, String chuTaiKhoan,
                                            Integer idNhanVien) {
        HoanTien ht = taoHoanTienChoXuLy(hoaDon, loai, soTien, yeuCau, tenNganHang, soTaiKhoan, chuTaiKhoan);
        HoanTatHoanTienRequest request = new HoanTatHoanTienRequest();
        request.setIdNhanVien(idNhanVien);
        return hoanTatInternal(ht, request);
    }

    /**
     * Admin xac nhan hoan tien.
     * - Co RefundGateway (VNPAY): goi API, tu dien maGiaoDichHoan.
     * - Khong co gateway (CHUYEN_KHOAN): yeu cau admin nhap maGiaoDichHoan.
     * Thanh cong: hoan_tien = DA_HOAN, hoa_don = TRA_HANG.
     */
    @Transactional
    public HoanTienResponse hoanTat(Integer id, HoanTatHoanTienRequest request) {
        HoanTien ht = hoanTienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu hoàn tiền.", "NOT_FOUND"));
        return hoanTatInternal(ht, request);
    }

    private HoanTienResponse hoanTatInternal(HoanTien ht, HoanTatHoanTienRequest request) {
        if (ht.getTrangThai() != TrangThaiHoanTien.CHO_XU_LY) {
            throw new ApiException("Yêu cầu hoàn tiền đã được xử lý.", "REFUND_ALREADY_PROCESSED");
        }
        if (request != null && request.getSoTien() != null
                && request.getSoTien().compareTo(BigDecimal.ZERO) >= 0) {
            ht.setSoTien(request.getSoTien());
        }

        Optional<RefundGateway> gatewayOpt = refundGatewayRegistry.getGatewayOptional(ht.getPhuongThuc());
        if (gatewayOpt.isPresent()) {
            thucHienHoanTuDong(ht, gatewayOpt.get(), request);
        } else {
            thucHienHoanThuCong(ht, request);
        }

        ht.setGhiChu(request != null ? request.getGhiChu() : ht.getGhiChu());
        ht.setIdNhanVien(resolveNhanVien(request != null ? request.getIdNhanVien() : null));
        ht.setTrangThai(TrangThaiHoanTien.DA_HOAN);
        ht.setNgayHoan(LocalDateTime.now());
        HoanTien saved = hoanTienRepository.save(ht);

        HoaDon hoaDon = saved.getIdHoaDon();
        if (saved.getLoai() == LoaiHoanTien.TRA_HANG) {
            capNhatHoaDonTraHang(hoaDon);
        }
        ghiNhatKy(hoaDon, "HOAN_TIEN_HOAN_TAT",
                "Đã hoàn tiền " + dinhDangTien(saved.getSoTien())
                        + (saved.getMaGiaoDichHoan() != null ? " - GD: " + saved.getMaGiaoDichHoan() : ""));
        thongBaoService.taoThongBao(
                LoaiThongBao.HOAN_TIEN_HOAN_TAT,
                "Đã hoàn tiền",
                "Đã hoàn tiền " + dinhDangTien(saved.getSoTien()) + " cho đơn " + hoaDon.getMaHoaDon() + ".",
                "/admin/hoan-tien",
                saved.getId(),
                hoaDon.getMaHoaDon());
        orderMailService.guiHoanTienHoanTat(hoaDon, saved.getSoTien(), saved.getMaGiaoDichHoan());
        return new HoanTienResponse(saved);
    }

    /** Admin tu choi hoan tien kem ly do. */
    @Transactional
    public HoanTienResponse tuChoi(Integer id, String lyDo, Integer idNhanVien) {
        HoanTien ht = hoanTienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu hoàn tiền.", "NOT_FOUND"));
        if (ht.getTrangThai() != TrangThaiHoanTien.CHO_XU_LY) {
            throw new ApiException("Yêu cầu hoàn tiền đã được xử lý.", "REFUND_ALREADY_PROCESSED");
        }
        ht.setTrangThai(TrangThaiHoanTien.TU_CHOI);
        ht.setGhiChu(lyDo);
        ht.setIdNhanVien(resolveNhanVien(idNhanVien));
        HoanTien saved = hoanTienRepository.save(ht);
        ghiNhatKy(saved.getIdHoaDon(), "HOAN_TIEN_TU_CHOI",
                "Từ chối hoàn tiền" + (lyDo != null && !lyDo.isBlank() ? ": " + lyDo : ""));
        return new HoanTienResponse(saved);
    }

    /** Khi hoan tien thanh cong: hoa don chuyen TRA_HANG (neu chua). */
    private void capNhatHoaDonTraHang(HoaDon hoaDon) {
        if (hoaDon == null) {
            return;
        }
        if (hoaDon.getTrangThai() != TrangThaiDonHang.TRA_HANG) {
            hoaDon.setTrangThai(TrangThaiDonHang.TRA_HANG);
            hoaDonRepository.save(hoaDon);
            ghiNhatKy(hoaDon, "TRA_HANG", "Đơn chuyển sang trả hàng sau khi hoàn tiền thành công");
        }
    }

    private void thucHienHoanTuDong(HoanTien ht, RefundGateway gateway, HoanTatHoanTienRequest request) {
        HoaDon hoaDon = ht.getIdHoaDon();
        ThanhToanHoaDon thanhToan = thanhToanHoaDonRepository
                .findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_THANH_CONG)
                .orElseThrow(() -> new ApiException(
                        "Không tìm thấy giao dịch thanh toán thành công để hoàn tiền.",
                        "PAYMENT_NOT_FOUND"));

        String providerPayDate = thanhToan.getProviderPayDate();
        if (providerPayDate == null || providerPayDate.isBlank()) {
            // Don cu chua luu vnp_PayDate: suy ra tu thoi_gian local (yyyyMMddHHmmss).
            if (thanhToan.getThoiGian() != null) {
                providerPayDate = thanhToan.getThoiGian()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
        }
        if (providerPayDate == null || providerPayDate.isBlank()) {
            throw new ApiException(
                    "Thiếu ngày thanh toán gốc từ nhà cung cấp. Không thể hoàn tiền tự động.",
                    "MISSING_PROVIDER_PAY_DATE");
        }

        NhanVien nv = resolveNhanVien(request != null ? request.getIdNhanVien() : null);
        String createBy = nv != null && nv.getHoTen() != null ? nv.getHoTen() : "admin";
        boolean fullRefund = thanhToan.getSoTien() != null
                && ht.getSoTien() != null
                && ht.getSoTien().compareTo(thanhToan.getSoTien()) == 0;

        RefundCommand command = RefundCommand.builder()
                .refundRequestId(generateRefundRequestId(ht.getId()))
                .originalTransactionRef(thanhToan.getMaGiaoDich())
                .providerTransactionNo(thanhToan.getProviderTransactionNo())
                .providerPayDate(providerPayDate)
                .amount(ht.getSoTien())
                .fullRefund(fullRefund)
                .orderInfo("Hoan tien hoa don " + hoaDon.getMaHoaDon())
                .createBy(createBy)
                .clientIp("127.0.0.1")
                .build();

        RefundResult result = gateway.refund(command);
        ht.setPhanHoiNcc(result.getRawResponse());

        if (!result.isSuccessful()) {
            throw new ApiException(
                    "Hoàn tiền " + gateway.getProviderCode() + " thất bại"
                            + (result.getMessage() != null ? ": " + result.getMessage() : ".")
                            + (result.getResponseCode() != null ? " (mã: " + result.getResponseCode() + ")" : ""),
                    "REFUND_PROVIDER_FAILED");
        }

        String maHoan = result.getProviderRefundNo();
        if (maHoan == null || maHoan.isBlank()) {
            maHoan = command.getRefundRequestId();
        }
        ht.setMaGiaoDichHoan(maHoan);
    }

    private void thucHienHoanThuCong(HoanTien ht, HoanTatHoanTienRequest request) {
        String maGiaoDich = request != null ? request.getMaGiaoDichHoan() : null;
        if (maGiaoDich == null || maGiaoDich.isBlank()) {
            throw new ApiException(
                    "Vui lòng nhập mã giao dịch hoàn tiền (chuyển khoản).",
                    "MISSING_REFUND_TRANSACTION_REF");
        }
        ht.setMaGiaoDichHoan(maGiaoDich.trim());
    }

    private String generateRefundRequestId(Integer hoanTienId) {
        // VNPay vnp_RequestId: toi da 50 ky tu; uu tien chu so de tranh loi checksum.
        return "RF" + hoanTienId + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    /** So tien hoan uu tien lay tu giao dich thanh toan thanh cong (dung voi so tien VNPay da thu). */
    public BigDecimal resolveSoTienHoan(HoaDon hoaDon) {
        if (hoaDon == null) {
            return BigDecimal.ZERO;
        }
        return thanhToanHoaDonRepository.findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_THANH_CONG)
                .map(ThanhToanHoaDon::getSoTien)
                .filter(s -> s != null && s.compareTo(BigDecimal.ZERO) > 0)
                .orElse(hoaDon.getThanhTien() != null ? hoaDon.getThanhTien() : BigDecimal.ZERO);
    }

    private String resolvePhuongThuc(HoaDon hoaDon) {
        boolean laVnpay = hoaDon.getIdPhuongThucThanhToan() != null
                && MA_VNPAY.equalsIgnoreCase(hoaDon.getIdPhuongThucThanhToan().getMa());
        return laVnpay ? PHUONG_THUC_VNPAY : PHUONG_THUC_CHUYEN_KHOAN;
    }

    private NhanVien resolveNhanVien(Integer idNhanVien) {
        if (idNhanVien == null) {
            return null;
        }
        return nhanVienRepository.findById(idNhanVien).orElse(null);
    }

    private void ghiNhatKy(HoaDon hoaDon, String trangThai, String ghiChu) {
        if (hoaDon == null) {
            return;
        }
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setIdHoaDon(hoaDon);
        lichSu.setTrangThai(trangThai);
        lichSu.setGhiChu(ghiChu != null && ghiChu.length() > 255 ? ghiChu.substring(0, 255) : ghiChu);
        lichSu.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(lichSu);
    }

    private String dinhDangTien(BigDecimal value) {
        BigDecimal v = value != null ? value : BigDecimal.ZERO;
        String s = v.setScale(0, java.math.RoundingMode.HALF_UP).toBigInteger().toString();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
            if (++count % 3 == 0 && i > 0) {
                sb.append('.');
            }
        }
        return sb.reverse() + "\u20ab";
    }
}
