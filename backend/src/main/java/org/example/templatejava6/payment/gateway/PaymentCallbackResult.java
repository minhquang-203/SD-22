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
    private String responseCode;
    private String message;
    private BigDecimal amount;
    private Map<String, String> rawParams;
}
