package org.example.templatejava6.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.templatejava6.payment.model.request.TaoThanhToanRequest;
import org.example.templatejava6.payment.model.response.KetQuaThanhToanResponse;
import org.example.templatejava6.payment.model.response.TaoThanhToanResponse;
import org.example.templatejava6.payment.model.response.VnpayIpnResponse;
import org.example.templatejava6.payment.service.PaymentService;
import org.example.templatejava6.payment.vnpay.VnpayGateway;
import org.example.templatejava6.payment.vnpay.VnpayProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final VnpayProperties vnpayProperties;

    public PaymentController(PaymentService paymentService, VnpayProperties vnpayProperties) {
        this.paymentService = paymentService;
        this.vnpayProperties = vnpayProperties;
    }

    @PostMapping("/{provider}/create")
    public TaoThanhToanResponse taoThanhToan(
            @PathVariable String provider,
            @Valid @RequestBody TaoThanhToanRequest request,
            HttpServletRequest servletRequest) {
        return paymentService.taoThanhToan(provider, request, getClientIp(servletRequest));
    }

    @GetMapping("/vnpay/callback")
    public RedirectView vnpayCallback(@RequestParam Map<String, String> params) {
        KetQuaThanhToanResponse result = paymentService.xuLyCallback(VnpayGateway.PROVIDER_CODE, params);
        return new RedirectView(buildFrontendRedirectUrl(result));
    }

    /**
     * IPN VNPay (GET, server-to-server). Phải khai báo URL này trên cổng merchant sandbox/production.
     * Không dùng để redirect trình duyệt — luôn trả JSON RspCode/Message.
     */
    @GetMapping("/vnpay/ipn")
    public VnpayIpnResponse vnpayIpn(@RequestParam Map<String, String> params) {
        return paymentService.xuLyIpn(params);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildFrontendRedirectUrl(KetQuaThanhToanResponse result) {
        String baseUrl = vnpayProperties.getFrontendReturnUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "/";
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator
                + "success=" + result.isSuccess()
                + "&provider=" + encode(result.getProvider())
                + "&orderId=" + encode(result.getIdHoaDon())
                + "&orderCode=" + encode(result.getMaHoaDon())
                + "&transactionRef=" + encode(result.getTransactionRef())
                + "&responseCode=" + encode(result.getResponseCode())
                + "&message=" + encode(result.getMessage());
    }

    private String encode(Object value) {
        return value == null ? "" : URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
