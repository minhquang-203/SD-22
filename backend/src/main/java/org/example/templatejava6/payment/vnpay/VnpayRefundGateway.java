package org.example.templatejava6.payment.vnpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.payment.gateway.RefundCommand;
import org.example.templatejava6.payment.gateway.RefundGateway;
import org.example.templatejava6.payment.gateway.RefundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Goi API hoan tien VNPay (vnp_Command=refund) qua merchant web API.
 * Chu ky dung hashdata noi bang '|', khac voi create payment (query string).
 */
@Component
public class VnpayRefundGateway implements RefundGateway {

    private static final Logger log = LoggerFactory.getLogger(VnpayRefundGateway.class);

    public static final String PROVIDER_CODE = "VNPAY";

    private static final String REFUND_COMMAND = "refund";
    private static final String FULL_REFUND_TYPE = "02";
    private static final String PARTIAL_REFUND_TYPE = "03";
    private static final String SUCCESS_RESPONSE_CODE = "00";
    private static final String DEFAULT_MERCHANT_API_URL =
            "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    private static final DateTimeFormatter VNPAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String sslTrustMode;

    public VnpayRefundGateway(VnpayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SslSetup sslSetup = buildSslSetup();
        this.sslTrustMode = sslSetup.mode();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .sslContext(sslSetup.sslContext())
                .build();
        // #region agent log
        agentLog("A", "VnpayRefundGateway.<init>", "HttpClient SSL init", Map.of(
                "sslTrustMode", sslTrustMode,
                "javaVersion", System.getProperty("java.version", ""),
                "javaHome", System.getProperty("java.home", ""),
                "trustStore", String.valueOf(System.getProperty("javax.net.ssl.trustStore")),
                "osName", System.getProperty("os.name", "")));
        // #endregion
    }

    @Override
    public String getProviderCode() {
        return PROVIDER_CODE;
    }

    @Override
    public RefundResult refund(RefundCommand command) {
        validateConfig();
        validateCommand(command);

        String requestId = command.getRefundRequestId();
        String createDate = LocalDateTime.now().format(VNPAY_DATE_TIME);
        String transactionType = command.isFullRefund() ? FULL_REFUND_TYPE : PARTIAL_REFUND_TYPE;
        String amount = toVnpayAmount(command.getAmount());
        String txnNo = nullToEmpty(command.getProviderTransactionNo());
        String orderInfo = sanitizeAscii(
                command.getOrderInfo() != null && !command.getOrderInfo().isBlank()
                        ? command.getOrderInfo()
                        : "Hoan tien giao dich " + command.getOriginalTransactionRef(),
                "Hoan tien VNPAY");
        String createBy = sanitizeAscii(
                command.getCreateBy() != null && !command.getCreateBy().isBlank()
                        ? command.getCreateBy()
                        : "admin",
                "admin");
        String ipAddr = command.getClientIp() != null && !command.getClientIp().isBlank()
                ? command.getClientIp()
                : "127.0.0.1";

        // Thu tu field bat buoc cua VNPay refund hashdata (noi bang '|')
        String hashData = String.join("|",
                requestId,
                properties.getVersion(),
                REFUND_COMMAND,
                properties.getTmnCode(),
                transactionType,
                command.getOriginalTransactionRef(),
                amount,
                txnNo,
                command.getProviderPayDate(),
                createBy,
                createDate,
                ipAddr,
                orderInfo);
        String secureHash = hmacSha512(properties.getHashSecret(), hashData);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId);
        body.put("vnp_Version", properties.getVersion());
        body.put("vnp_Command", REFUND_COMMAND);
        body.put("vnp_TmnCode", properties.getTmnCode());
        body.put("vnp_TransactionType", transactionType);
        body.put("vnp_TxnRef", command.getOriginalTransactionRef());
        body.put("vnp_Amount", amount);
        body.put("vnp_TransactionNo", txnNo);
        body.put("vnp_TransactionDate", command.getProviderPayDate());
        body.put("vnp_CreateBy", createBy);
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", ipAddr);
        body.put("vnp_OrderInfo", orderInfo);
        body.put("vnp_SecureHash", secureHash);

        String apiUrl = resolveMerchantApiUrl();
        String rawResponse = postJson(apiUrl, body);
        return parseResponse(rawResponse);
    }

    private String postJson(String apiUrl, Map<String, String> body) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception ex) {
            throw new ApiException("Không tạo được payload hoàn tiền VNPAY.", "VNPAY_REFUND_SERIALIZE_FAILED");
        }

        try {
            // #region agent log
            agentLog("A", "VnpayRefundGateway.postJson:before", "About to call VNPay", Map.of(
                    "apiUrl", apiUrl,
                    "sslTrustMode", sslTrustMode,
                    "javaVersion", System.getProperty("java.version", ""),
                    "trustStore", String.valueOf(System.getProperty("javax.net.ssl.trustStore"))));
            // #endregion

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            log.info("Gọi VNPAY refund API: {}", apiUrl);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseBody = response.body() != null ? response.body() : "";

            // #region agent log
            agentLog("A", "VnpayRefundGateway.postJson:after", "VNPay HTTP response", Map.of(
                    "statusCode", response.statusCode(),
                    "bodyPreview", truncate(responseBody, 200),
                    "sslTrustMode", sslTrustMode));
            // #endregion

            // VNPay thuong tra HTTP 200 kem vnp_ResponseCode; van chap nhan body khi status khac 2xx.
            if (response.statusCode() >= 400 && responseBody.isBlank()) {
                throw new ApiException(
                        "VNPay trả HTTP " + response.statusCode() + " khi gọi " + apiUrl,
                        "VNPAY_REFUND_HTTP_FAILED");
            }
            if (response.statusCode() >= 400) {
                log.warn("VNPay refund HTTP {} body={}", response.statusCode(), truncate(responseBody, 300));
            }
            return responseBody;
        } catch (ApiException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    "Bị gián đoạn khi gọi API hoàn tiền VNPAY (" + apiUrl + ").",
                    "VNPAY_REFUND_HTTP_FAILED");
        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            // #region agent log
            agentLog("A", "VnpayRefundGateway.postJson:error", "VNPay call failed", Map.of(
                    "exceptionClass", root.getClass().getName(),
                    "exceptionMessage", String.valueOf(root.getMessage()),
                    "isCertPath", String.valueOf(
                            root.getClass().getSimpleName().contains("CertPath")
                                    || String.valueOf(root.getMessage()).contains("certification path")),
                    "sslTrustMode", sslTrustMode,
                    "javaHome", System.getProperty("java.home", "")));
            // #endregion
            throw new ApiException(
                    "Không gọi được API hoàn tiền VNPAY (" + apiUrl + "): "
                            + root.getClass().getSimpleName() + " - " + root.getMessage(),
                    "VNPAY_REFUND_HTTP_FAILED");
        }
    }

    /**
     * Ghep JVM cacerts + Windows-ROOT de khac phuc SunCertPathBuilderException
     * khi OS (PowerShell) tin cert nhung Java cacerts thieu CA trung gian.
     */
    private SslSetup buildSslSetup() {
        try {
            X509TrustManager jvmTm = defaultTrustManager();
            X509TrustManager windowsTm = windowsRootTrustManager();
            X509TrustManager combined = windowsTm == null ? jvmTm : combine(jvmTm, windowsTm);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{combined}, null);
            String mode = windowsTm == null ? "jvm-default" : "jvm+windows-root";
            log.info("VNPAY refund SSL trust mode: {}", mode);
            return new SslSetup(sslContext, mode);
        } catch (Exception ex) {
            log.warn("Khong tao duoc composite SSLContext, dung mac dinh JVM: {}", ex.getMessage());
            try {
                return new SslSetup(SSLContext.getDefault(), "jvm-fallback-default");
            } catch (Exception ignored) {
                throw new IllegalStateException("Cannot initialize SSLContext", ex);
            }
        }
    }

    private static X509TrustManager defaultTrustManager() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        return firstX509(tmf.getTrustManagers());
    }

    private static X509TrustManager windowsRootTrustManager() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return null;
        }
        try {
            KeyStore windowsRoot = KeyStore.getInstance("Windows-ROOT");
            windowsRoot.load(null, null);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(windowsRoot);
            return firstX509(tmf.getTrustManagers());
        } catch (Exception ex) {
            return null;
        }
    }

    private static X509TrustManager firstX509(TrustManager[] managers) {
        for (TrustManager tm : managers) {
            if (tm instanceof X509TrustManager x509) {
                return x509;
            }
        }
        throw new IllegalStateException("No X509TrustManager found");
    }

    private static X509TrustManager combine(X509TrustManager first, X509TrustManager second) {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try {
                    first.checkClientTrusted(chain, authType);
                } catch (CertificateException ex) {
                    second.checkClientTrusted(chain, authType);
                }
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try {
                    first.checkServerTrusted(chain, authType);
                } catch (CertificateException ex) {
                    second.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                List<X509Certificate> all = new ArrayList<>();
                all.addAll(Arrays.asList(first.getAcceptedIssuers()));
                all.addAll(Arrays.asList(second.getAcceptedIssuers()));
                return all.toArray(X509Certificate[]::new);
            }
        };
    }

    private record SslSetup(SSLContext sslContext, String mode) {
    }

    // #region agent log
    private void agentLog(String hypothesisId, String location, String message, Map<String, ?> data) {
        try {
            StringBuilder dataJson = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, ?> e : data.entrySet()) {
                if (!first) {
                    dataJson.append(',');
                }
                first = false;
                dataJson.append('"').append(e.getKey()).append("\":")
                        .append(objectMapper.writeValueAsString(String.valueOf(e.getValue())));
            }
            dataJson.append('}');
            String line = "{\"sessionId\":\"bb7b9b\",\"runId\":\"post-fix\",\"hypothesisId\":\""
                    + hypothesisId + "\",\"location\":\"" + location + "\",\"message\":\""
                    + message.replace("\"", "'") + "\",\"data\":" + dataJson
                    + ",\"timestamp\":" + System.currentTimeMillis() + "}\n";
            try (FileWriter fw = new FileWriter("d:\\Desktop\\SD-22\\debug-bb7b9b.log", true)) {
                fw.write(line);
            }
        } catch (Exception ignored) {
            // debug logging must not break refund flow
        }
    }
    // #endregion

    private RefundResult parseResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String responseCode = text(root, "vnp_ResponseCode");
            String message = text(root, "vnp_Message");
            if (message == null || message.isBlank()) {
                message = SUCCESS_RESPONSE_CODE.equals(responseCode)
                        ? "Hoàn tiền VNPAY thành công."
                        : "Hoàn tiền VNPAY thất bại (" + responseCode + ").";
            }
            String providerRefundNo = text(root, "vnp_TransactionNo");
            if (providerRefundNo == null || providerRefundNo.isBlank()) {
                providerRefundNo = text(root, "vnp_TxnRef");
            }
            return RefundResult.builder()
                    .successful(SUCCESS_RESPONSE_CODE.equals(responseCode))
                    .providerRefundNo(providerRefundNo)
                    .responseCode(responseCode)
                    .message(message)
                    .rawResponse(truncate(rawResponse, 500))
                    .build();
        } catch (Exception ex) {
            return RefundResult.builder()
                    .successful(false)
                    .responseCode(null)
                    .message("Không parse được phản hồi VNPAY: " + ex.getMessage())
                    .rawResponse(truncate(rawResponse, 500))
                    .build();
        }
    }

    private void validateCommand(RefundCommand command) {
        if (command == null) {
            throw new ApiException("Thiếu dữ liệu yêu cầu hoàn tiền.", "INVALID_REFUND_COMMAND");
        }
        if (isBlank(command.getRefundRequestId())) {
            throw new ApiException("Thiếu mã yêu cầu hoàn tiền.", "INVALID_REFUND_REQUEST_ID");
        }
        if (isBlank(command.getOriginalTransactionRef())) {
            throw new ApiException("Thiếu mã giao dịch gốc để hoàn tiền VNPAY.", "MISSING_ORIGINAL_TXN_REF");
        }
        if (isBlank(command.getProviderPayDate())) {
            throw new ApiException("Thiếu ngày thanh toán gốc để hoàn tiền VNPAY.", "MISSING_PROVIDER_PAY_DATE");
        }
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Số tiền hoàn không hợp lệ.", "INVALID_REFUND_AMOUNT");
        }
    }

    private void validateConfig() {
        if (isBlank(properties.getTmnCode()) || isBlank(properties.getHashSecret())
                || properties.getTmnCode().startsWith("YOUR_")
                || properties.getHashSecret().startsWith("YOUR_")) {
            throw new ApiException("Chưa cấu hình thông tin VNPAY refund.", "PAYMENT_CONFIG_MISSING");
        }
        resolveMerchantApiUrl();
    }

    private String resolveMerchantApiUrl() {
        String url = properties.getMerchantApiUrl();
        if (isBlank(url)) {
            url = DEFAULT_MERCHANT_API_URL;
        }
        url = url.trim();
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("URI thiếu scheme/host");
            }
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Scheme không hỗ trợ: " + uri.getScheme());
            }
            return url;
        } catch (Exception ex) {
            throw new ApiException(
                    "URL merchant VNPAY không hợp lệ: '" + url + "'. "
                            + "Đặt payment.vnpay.merchant-api-url=" + DEFAULT_MERCHANT_API_URL,
                    "PAYMENT_CONFIG_INVALID_URL");
        }
    }

    private String toVnpayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
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
            throw new ApiException("Không thể ký dữ liệu hoàn tiền VNPAY.", "PAYMENT_SIGN_FAILED");
        }
    }

    /** VNPay hash/createBy de bi loi neu co tieng Viet / ky tu dac biet. */
    private String sanitizeAscii(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String cleaned = value.replaceAll("[^\\x20-\\x7E]", "").trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
