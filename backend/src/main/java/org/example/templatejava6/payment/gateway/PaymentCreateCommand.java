package org.example.templatejava6.payment.gateway;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentCreateCommand {

    private String transactionRef;
    private String orderCode;
    private String orderInfo;
    private BigDecimal amount;
    private String clientIp;
}
