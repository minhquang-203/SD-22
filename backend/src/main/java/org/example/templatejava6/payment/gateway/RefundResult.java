package org.example.templatejava6.payment.gateway;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefundResult {

    private boolean successful;
    private String providerRefundNo;
    private String responseCode;
    private String message;
    private String rawResponse;
}
