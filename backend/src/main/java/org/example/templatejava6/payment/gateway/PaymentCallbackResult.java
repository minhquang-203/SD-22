package org.example.templatejava6.payment.gateway;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class PaymentCallbackResult {

    private boolean validSignature;
    private boolean successful;
    private String transactionRef;
    private String providerTransactionNo;
    /** Ngay thanh toan goc (vd: vnp_PayDate yyyyMMddHHmmss) — can de goi API refund. */
    private String providerPayDate;
    private String responseCode;
    private String message;
    private BigDecimal amount;
    private Map<String, String> rawParams;
}
