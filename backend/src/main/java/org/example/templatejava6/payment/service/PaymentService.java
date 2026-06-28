package org.example.templatejava6.payment.service;

import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.PhuongThucThanhToanRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.order.service.OnlineOrderLifecycleService;
import org.example.templatejava6.payment.gateway.PaymentCallbackResult;
import org.example.templatejava6.payment.gateway.PaymentCreateCommand;
import org.example.templatejava6.payment.gateway.PaymentCreateResult;
import org.example.templatejava6.payment.gateway.PaymentGateway;
import org.example.templatejava6.payment.gateway.PaymentGatewayRegistry;
import org.example.templatejava6.payment.model.request.TaoThanhToanRequest;
import org.example.templatejava6.payment.model.response.KetQuaThanhToanResponse;
import org.example.templatejava6.payment.model.response.TaoThanhToanResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private static final String TRANG_THAI_CHO_THANH_TOAN = "CHO_THANH_TOAN";
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";
    private static final String TRANG_THAI_THAT_BAI = "THAT_BAI";
    private static final String LOAI_DON_ONLINE = "ONLINE";
    private static final DateTimeFormatter TRANSACTION_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentGatewayRegistry gatewayRegistry;
    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final OnlineOrderLifecycleService onlineOrderLifecycleService;

    public PaymentService(
            PaymentGatewayRegistry gatewayRegistry,
            HoaDonRepository hoaDonRepository,
            ThanhToanHoaDonRepository thanhToanHoaDonRepository,
            PhuongThucThanhToanRepository phuongThucThanhToanRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            OnlineOrderLifecycleService onlineOrderLifecycleService) {
        this.gatewayRegistry = gatewayRegistry;
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.onlineOrderLifecycleService = onlineOrderLifecycleService;
    }

    @Transactional
    public TaoThanhToanResponse taoThanhToan(String providerCode, TaoThanhToanRequest request, String clientIp) {
        PaymentGateway gateway = gatewayRegistry.getGateway(providerCode);
        String provider = normalizeProvider(providerCode);
        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn.", "NOT_FOUND"));
        validateHoaDonCoTheThanhToan(hoaDon);

        PhuongThucThanhToan phuongThuc = resolvePhuongThucThanhToan(provider);
        hoaDon.setIdPhuongThucThanhToan(phuongThuc);
        hoaDonRepository.save(hoaDon);

        String transactionRef = generateTransactionRef(provider, hoaDon.getId());
        ThanhToanHoaDon thanhToan = new ThanhToanHoaDon();
        thanhToan.setIdHoaDon(hoaDon);
        thanhToan.setIdPhuongThucThanhToan(phuongThuc);
        thanhToan.setSoTien(hoaDon.getThanhTien());
        thanhToan.setMaGiaoDich(transactionRef);
        thanhToan.setTrangThai(TRANG_THAI_CHO_THANH_TOAN);
        thanhToan.setThoiGian(LocalDateTime.now());
        thanhToanHoaDonRepository.save(thanhToan);

        PaymentCreateResult result = gateway.createPayment(PaymentCreateCommand.builder()
                .transactionRef(transactionRef)
                .orderCode(hoaDon.getMaHoaDon())
                .orderInfo("Thanh toan hoa don " + hoaDon.getMaHoaDon())
                .amount(hoaDon.getThanhTien())
                .clientIp(clientIp)
                .build());

        ghiNhatKy(hoaDon, "CHO_THANH_TOAN",
                "Tạo yêu cầu thanh toán " + provider + " - " + transactionRef);

        return TaoThanhToanResponse.builder()
                .provider(result.getProvider())
                .idHoaDon(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .transactionRef(result.getTransactionRef())
                .paymentUrl(result.getPaymentUrl())
                .build();
    }

    @Transactional
    public KetQuaThanhToanResponse xuLyCallback(String providerCode, Map<String, String> params) {
        PaymentGateway gateway = gatewayRegistry.getGateway(providerCode);
        String provider = normalizeProvider(providerCode);
        PaymentCallbackResult callback = gateway.verifyCallback(params);

        if (!callback.isValidSignature()) {
            return buildCallbackResponse(callback, provider, null, false, "Sai chữ ký thanh toán.");
        }

        ThanhToanHoaDon thanhToan = thanhToanHoaDonRepository.findByMaGiaoDich(callback.getTransactionRef())
                .orElseThrow(() -> new ApiException("Không tìm thấy giao dịch thanh toán.", "PAYMENT_NOT_FOUND"));
        HoaDon hoaDon = thanhToan.getIdHoaDon();

        if (TRANG_THAI_THANH_CONG.equals(thanhToan.getTrangThai())) {
            return buildCallbackResponse(callback, provider, hoaDon, true, "Giao dịch đã được ghi nhận trước đó.");
        }

        if (!amountMatches(hoaDon.getThanhTien(), callback.getAmount())) {
            thanhToan.setTrangThai(TRANG_THAI_THAT_BAI);
            thanhToan.setThoiGian(LocalDateTime.now());
            thanhToanHoaDonRepository.save(thanhToan);
            ghiNhatKy(hoaDon, "THANH_TOAN_THAT_BAI", "Số tiền VNPAY trả về không khớp hóa đơn.");
            return buildCallbackResponse(callback, provider, hoaDon, false, "Số tiền thanh toán không khớp hóa đơn.");
        }

        if (callback.isSuccessful()) {
            if (hoaDon.getTrangThai() == TrangThaiDonHang.DA_HUY) {
                thanhToan.setTrangThai(TRANG_THAI_THAT_BAI);
                thanhToan.setThoiGian(LocalDateTime.now());
                thanhToanHoaDonRepository.save(thanhToan);
                ghiNhatKy(hoaDon, "THANH_TOAN_QUA_HAN",
                        "VNPAY trả về thành công nhưng đơn đã hết hạn hoặc đã hủy.");
                return buildCallbackResponse(callback, provider, hoaDon, false, "Đơn hàng đã hết hạn hoặc đã hủy.");
            }
            thanhToan.setTrangThai(TRANG_THAI_THANH_CONG);
            thanhToan.setThoiGian(LocalDateTime.now());
            thanhToanHoaDonRepository.save(thanhToan);

            if (hoaDon.getTrangThai() == TrangThaiDonHang.CHO_XAC_NHAN) {
                hoaDon.setTrangThai(TrangThaiDonHang.DA_XAC_NHAN);
                hoaDonRepository.save(hoaDon);
            }
            ghiNhatKy(hoaDon, "THANH_TOAN",
                    "Thanh toán " + provider + " thành công"
                            + formatProviderTransaction(callback.getProviderTransactionNo()));
            return buildCallbackResponse(callback, provider, hoaDon, true, callback.getMessage());
        }

        thanhToan.setTrangThai(TRANG_THAI_THAT_BAI);
        thanhToan.setThoiGian(LocalDateTime.now());
        thanhToanHoaDonRepository.save(thanhToan);
        onlineOrderLifecycleService.huyDonOnline(hoaDon, "Thanh toán " + provider + " thất bại, hủy đơn và hoàn tồn.");
        ghiNhatKy(hoaDon, "THANH_TOAN_THAT_BAI",
                "Thanh toán " + provider + " thất bại. Mã phản hồi: " + callback.getResponseCode());
        return buildCallbackResponse(callback, provider, hoaDon, false, callback.getMessage());
    }

    private void validateHoaDonCoTheThanhToan(HoaDon hoaDon) {
        if (hoaDon.getThanhTien() == null || hoaDon.getThanhTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Hóa đơn không có số tiền cần thanh toán.", "INVALID_PAYMENT_AMOUNT");
        }
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai().laTrangThaiKetThuc()) {
            throw new ApiException("Hóa đơn không thể thanh toán ở trạng thái hiện tại.", "INVALID_ORDER_STATUS");
        }
        if (hoaDon.getLoaiDon() != null && !LOAI_DON_ONLINE.equalsIgnoreCase(hoaDon.getLoaiDon())) {
            throw new ApiException("Chỉ hỗ trợ thanh toán online cho hóa đơn ONLINE.", "INVALID_ORDER_TYPE");
        }
        thanhToanHoaDonRepository.findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_THANH_CONG)
                .ifPresent(thanhToan -> {
                    throw new ApiException("Hóa đơn đã thanh toán thành công.", "ORDER_ALREADY_PAID");
                });
    }

    private PhuongThucThanhToan resolvePhuongThucThanhToan(String provider) {
        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository.findByMaIgnoreCase(provider)
                .orElseThrow(() -> new ApiException("Chưa cấu hình phương thức thanh toán " + provider + ".", "PAYMENT_METHOD_NOT_FOUND"));
        if (!Boolean.TRUE.equals(phuongThuc.getTrangThai())) {
            throw new ApiException("Phương thức thanh toán không còn hoạt động.", "INACTIVE_PAYMENT");
        }
        return phuongThuc;
    }

    private KetQuaThanhToanResponse buildCallbackResponse(
            PaymentCallbackResult callback,
            String provider,
            HoaDon hoaDon,
            boolean success,
            String message) {
        return KetQuaThanhToanResponse.builder()
                .success(success)
                .provider(provider)
                .idHoaDon(hoaDon != null ? hoaDon.getId() : null)
                .maHoaDon(hoaDon != null ? hoaDon.getMaHoaDon() : null)
                .transactionRef(callback.getTransactionRef())
                .providerTransactionNo(callback.getProviderTransactionNo())
                .responseCode(callback.getResponseCode())
                .message(message)
                .build();
    }

    private boolean amountMatches(BigDecimal expected, BigDecimal actual) {
        return expected != null && actual != null && expected.compareTo(actual) == 0;
    }

    private String generateTransactionRef(String provider, Integer hoaDonId) {
        return provider + "-" + hoaDonId + "-" + LocalDateTime.now().format(TRANSACTION_TIME)
                + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String normalizeProvider(String providerCode) {
        return providerCode == null ? "" : providerCode.trim().toUpperCase(Locale.ROOT);
    }

    private void ghiNhatKy(HoaDon hoaDon, String trangThai, String ghiChu) {
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setIdHoaDon(hoaDon);
        lichSu.setTrangThai(trangThai);
        lichSu.setGhiChu(ghiChu);
        lichSu.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(lichSu);
    }

    private String formatProviderTransaction(String providerTransactionNo) {
        return providerTransactionNo == null || providerTransactionNo.isBlank()
                ? ""
                : " (" + providerTransactionNo + ")";
    }
}
