package org.example.templatejava6.payment.vnpay;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.payment.gateway.PaymentCallbackResult;
import org.example.templatejava6.payment.gateway.PaymentCreateCommand;
import org.example.templatejava6.payment.gateway.PaymentCreateResult;
import org.example.templatejava6.payment.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class VnpayGateway implements PaymentGateway {

    public static final String PROVIDER_CODE = "VNPAY";
    private static final DateTimeFormatter VNPAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String SUCCESS_RESPONSE_CODE = "00";

    private final VnpayProperties properties;

    public VnpayGateway(VnpayProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getProviderCode() {
        return PROVIDER_CODE;
    }

    @Override
    public PaymentCreateResult createPayment(PaymentCreateCommand command) {
        validateConfig();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", toVnpayAmount(command.getAmount()));
        params.put("vnp_CurrCode", properties.getCurrCode());
        params.put("vnp_TxnRef", command.getTransactionRef());
        params.put("vnp_OrderInfo", command.getOrderInfo());
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", command.getClientIp());
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNPAY_DATE_TIME));

        String query = buildQuery(params);
        String secureHash = hmacSha512(properties.getHashSecret(), query);
        String paymentUrl = properties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        return PaymentCreateResult.builder()
                .provider(PROVIDER_CODE)
                .transactionRef(command.getTransactionRef())
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    public PaymentCallbackResult verifyCallback(Map<String, String> params) {
        validateConfig();

        Map<String, String> signedParams = new TreeMap<>();
        params.forEach((key, value) -> {
            if (value != null && !value.isBlank()
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)) {
                signedParams.put(key, value);
            }
        });

        String expectedHash = hmacSha512(properties.getHashSecret(), buildQuery(signedParams));
        String actualHash = params.get("vnp_SecureHash");
        boolean validSignature = actualHash != null && expectedHash.equalsIgnoreCase(actualHash);
        String responseCode = params.get("vnp_ResponseCode");

        return PaymentCallbackResult.builder()
                .validSignature(validSignature)
                .successful(validSignature && SUCCESS_RESPONSE_CODE.equals(responseCode))
                .transactionRef(params.get("vnp_TxnRef"))
                .providerTransactionNo(params.get("vnp_TransactionNo"))
                .providerPayDate(params.get("vnp_PayDate"))
                .responseCode(responseCode)
                .message(toMessage(responseCode, validSignature))
                .amount(fromVnpayAmount(params.get("vnp_Amount")))
                .rawParams(params)
                .build();
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String toVnpayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private BigDecimal fromVnpayAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            return null;
        }
        return new BigDecimal(amount).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new ApiException("Không thể ký dữ liệu thanh toán VNPAY.", "PAYMENT_SIGN_FAILED");
        }
    }

    private String toMessage(String responseCode, boolean validSignature) {
        if (!validSignature) {
            return "Sai chữ ký VNPAY.";
        }
        return SUCCESS_RESPONSE_CODE.equals(responseCode)
                ? "Thanh toán VNPAY thành công."
                : "Thanh toán VNPAY không thành công.";
    }

    private void validateConfig() {
        if (isBlank(properties.getTmnCode()) || isBlank(properties.getHashSecret())
                || isBlank(properties.getPayUrl()) || isBlank(properties.getReturnUrl())
                || properties.getTmnCode().startsWith("YOUR_")
                || properties.getHashSecret().startsWith("YOUR_")) {
            throw new ApiException("Chưa cấu hình thông tin VNPAY.", "PAYMENT_CONFIG_MISSING");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
