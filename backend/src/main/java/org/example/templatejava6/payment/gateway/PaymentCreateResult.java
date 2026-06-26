package org.example.templatejava6.payment.gateway;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCreateResult {

    private String provider;
    private String transactionRef;
    private String paymentUrl;
}
