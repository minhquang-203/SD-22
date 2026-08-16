package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.enums.LoaiHoanTien;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.enums.TrangThaiHoanTien;
import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.service.ProductFileStorageService;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.OrderMailService;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.order.entity.AnhHoanTien;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoanTien;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.model.request.HoanTatHoanTienRequest;
import org.example.templatejava6.order.model.response.HoanTienResponse;
import org.example.templatejava6.order.repository.AnhHoanTienRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.HoanTienRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.order.repository.YeuCauTraHangRepository;
import org.example.templatejava6.payment.gateway.RefundCommand;
import org.example.templatejava6.payment.gateway.RefundGateway;
import org.example.templatejava6.payment.gateway.RefundGatewayRegistry;
import org.example.templatejava6.payment.gateway.RefundResult;
import org.example.templatejava6.realtime.service.OrderRealtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quan ly hoan tien: tao ban ghi CHO_XU_LY khi huy don, hoac khi shop da nhan lai hang tra.
 * Hoan tien luon do admin quyet dinh — khong bao gio tu dong goi cong thanh toan.
 * Khi admin xac nhan: co RefundGateway (VNPAY) thi goi API tu dong, nguoc lai (CHUYEN_KHOAN)
 * admin nhap ma giao dich thu cong. Thanh cong: hoan_tien = DA_HOAN va hoa don = TRA_HANG.
 * Voi hoan tien tra hang, ca hai quyet dinh (hoan tat / tu choi) deu dong yeu cau tra hang sang HOAN_TAT.
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private static final String MA_VNPAY = "VNPAY";
    private static final String PHUONG_THUC_VNPAY = "VNPAY";
    private static final String PHUONG_THUC_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";

    private final HoanTienRepository hoanTienRepository;
    private final AnhHoanTienRepository anhHoanTienRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final YeuCauTraHangRepository yeuCauTraHangRepository;
    private final RefundGatewayRegistry refundGatewayRegistry;
    private final ProductFileStorageService productFileStorageService;
    private final ThongBaoService thongBaoService;
    private final OrderMailService orderMailService;
    private final OrderRealtimeService orderRealtimeService;

    public RefundService(HoanTienRepository hoanTienRepository,
                         AnhHoanTienRepository anhHoanTienRepository,
                         HoaDonRepository hoaDonRepository,
                         ThanhToanHoaDonRepository thanhToanHoaDonRepository,
                         LichSuDonHangRepository lichSuDonHangRepository,
                         NhanVienRepository nhanVienRepository,
                         YeuCauTraHangRepository yeuCauTraHangRepository,
                         RefundGatewayRegistry refundGatewayRegistry,
                         ProductFileStorageService productFileStorageService,
                         ThongBaoService thongBaoService,
                         OrderMailService orderMailService,
                         OrderRealtimeService orderRealtimeService) {
        this.hoanTienRepository = hoanTienRepository;
        this.anhHoanTienRepository = anhHoanTienRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.yeuCauTraHangRepository = yeuCauTraHangRepository;
        this.refundGatewayRegistry = refundGatewayRegistry;
        this.productFileStorageService = productFileStorageService;
        this.thongBaoService = thongBaoService;
        this.orderMailService = orderMailService;
        this.orderRealtimeService = orderRealtimeService;
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
        return list.stream().map(this::toResponse).toList();
    }

    /**
     * Tao ban ghi hoan tien tra hang cho admin quyet dinh, bo qua neu don da co ban ghi hoan tien
     * dang cho / da hoan. Dung khi shop nhan lai hang tra.
     */
    @Transactional
    public HoanTien taoHoanTienTraHangNeuChua(HoaDon hoaDon, BigDecimal soTien, YeuCauTraHang yeuCau,
                                              String tenNganHang, String soTaiKhoan, String chuTaiKhoan) {
        if (hoaDon == null) {
            return null;
        }
        if (hoanTienRepository.existsByIdHoaDon_IdAndTrangThaiNot(hoaDon.getId(), TrangThaiHoanTien.TU_CHOI)) {
            log.info("Đơn {} đã có bản ghi hoàn tiền, bỏ qua tạo mới khi nhận hàng trả.",
                    hoaDon.getMaHoaDon());
            return null;
        }
        return taoHoanTienChoXuLy(
                hoaDon, LoaiHoanTien.TRA_HANG, soTien, yeuCau, tenNganHang, soTaiKhoan, chuTaiKhoan);
    }

    /**
     * Admin xac nhan hoan tien.
     * - Co RefundGateway (VNPAY): goi API, tu dien maGiaoDichHoan.
     * - Khong co gateway (CHUYEN_KHOAN/COD): yeu cau admin nhap maGiaoDichHoan, co the dinh kem anh.
     * Thanh cong: hoan_tien = DA_HOAN, hoa_don = TRA_HANG.
     */
    @Transactional
    public HoanTienResponse hoanTat(Integer id, HoanTatHoanTienRequest request, List<MultipartFile> files) {
        HoanTien ht = hoanTienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu hoàn tiền.", "NOT_FOUND"));
        return hoanTatInternal(ht, request, files);
    }

    private HoanTienResponse hoanTatInternal(HoanTien ht, HoanTatHoanTienRequest request,
                                             List<MultipartFile> files) {
        if (ht.getTrangThai() != TrangThaiHoanTien.CHO_XU_LY) {
            throw new ApiException("Yêu cầu hoàn tiền đã được xử lý.", "REFUND_ALREADY_PROCESSED");
        }
        // VNPAY co the dieu chinh so tien; COD/chuyen khoan giu so tien da tao san.
        boolean laVnpay = PHUONG_THUC_VNPAY.equalsIgnoreCase(ht.getPhuongThuc());
        if (laVnpay && request != null && request.getSoTien() != null
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

        List<String> anhUrls = luuAnhChungTu(saved, files);

        HoaDon hoaDon = saved.getIdHoaDon();
        if (saved.getLoai() == LoaiHoanTien.TRA_HANG) {
            capNhatHoaDonTraHang(hoaDon);
            ketThucYeuCauTraHang(saved.getIdYeuCauTraHang());
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
        thongBaoService.taoThongBaoKhach(
                idKhachHangCua(hoaDon),
                LoaiThongBao.HOAN_TIEN_THANH_CONG,
                "Hoàn tiền thành công",
                "Đơn " + hoaDon.getMaHoaDon() + " đã được hoàn "
                        + dinhDangTien(saved.getSoTien()) + ".",
                linkKhachHoanTien(saved),
                hoaDon.getId(),
                hoaDon.getMaHoaDon());
        return new HoanTienResponse(saved, anhUrls);
    }

    private HoanTienResponse toResponse(HoanTien ht) {
        List<String> anhUrls = anhHoanTienRepository
                .findByIdHoanTien_IdOrderByIdAsc(ht.getId())
                .stream()
                .map(AnhHoanTien::getDuongDan)
                .toList();
        return new HoanTienResponse(ht, anhUrls);
    }

    private List<String> luuAnhChungTu(HoanTien hoanTien, List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return urls;
        }
        LocalDateTime now = LocalDateTime.now();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String path = productFileStorageService.store(file);
            AnhHoanTien anh = new AnhHoanTien();
            anh.setIdHoanTien(hoanTien);
            anh.setDuongDan(path);
            anh.setNgayTao(now);
            anhHoanTienRepository.save(anh);
            urls.add(path);
        }
        return urls;
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
        if (saved.getLoai() == LoaiHoanTien.TRA_HANG) {
            ketThucYeuCauTraHang(saved.getIdYeuCauTraHang());
        }
        HoaDon hoaDon = saved.getIdHoaDon();
        thongBaoService.taoThongBaoKhach(
                idKhachHangCua(hoaDon),
                LoaiThongBao.HOAN_TIEN_BI_TU_CHOI,
                "Hoàn tiền bị từ chối",
                "Yêu cầu hoàn tiền cho đơn " + (hoaDon != null ? hoaDon.getMaHoaDon() : "") + " đã bị từ chối"
                        + (lyDo != null && !lyDo.isBlank() ? ": " + lyDo : "."),
                linkKhachHoanTien(saved),
                hoaDon != null ? hoaDon.getId() : null,
                hoaDon != null ? hoaDon.getMaHoaDon() : null);
        return new HoanTienResponse(saved);
    }

    /**
     * Dong yeu cau tra hang sau khi admin da quyet dinh hoan tien hay khong:
     * {@code DA_NHAN_HANG} -> {@code HOAN_TAT}.
     */
    private void ketThucYeuCauTraHang(YeuCauTraHang yeuCau) {
        if (yeuCau == null || yeuCau.getTrangThai() != TrangThaiTraHang.DA_NHAN_HANG) {
            return;
        }
        yeuCau.setTrangThai(TrangThaiTraHang.HOAN_TAT);
        yeuCau.setNgayCapNhat(LocalDateTime.now());
        yeuCauTraHangRepository.save(yeuCau);
    }

    /** Khi hoan tien thanh cong: hoa don chuyen TRA_HANG (neu chua). */
    private void capNhatHoaDonTraHang(HoaDon hoaDon) {
        if (hoaDon == null) {
            return;
        }
        if (hoaDon.getTrangThai() != TrangThaiDonHang.TRA_HANG) {
            TrangThaiDonHang trangThaiCu = hoaDon.getTrangThai();
            hoaDon.setTrangThai(TrangThaiDonHang.TRA_HANG);
            hoaDonRepository.save(hoaDon);
            ghiNhatKy(hoaDon, "TRA_HANG", "Đơn chuyển sang trả hàng sau khi hoàn tiền thành công");
            orderRealtimeService.publishStatusChanged(hoaDon, trangThaiCu);
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

    private static Integer idKhachHangCua(HoaDon hoaDon) {
        return hoaDon != null && hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;
    }

    private static String linkKhachHoanTien(HoanTien ht) {
        if (ht != null && ht.getIdYeuCauTraHang() != null && ht.getIdYeuCauTraHang().getId() != null) {
            return "/tra-cuu-don/tra-hang/" + ht.getIdYeuCauTraHang().getId();
        }
        return "/tra-cuu-don";
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
