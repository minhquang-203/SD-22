package org.example.templatejava6.payment.gateway;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RefundCommand {

    /** Ma yeu cau hoan tien noi bo (unique per request). */
    private String refundRequestId;

    /** Ma giao dich goc (vd: vnp_TxnRef). */
    private String originalTransactionRef;

    /** Ma giao dich nha cung cap (vd: vnp_TransactionNo). */
    private String providerTransactionNo;

    /** Ngay thanh toan goc (vd: vnp_PayDate / vnp_TransactionDate). */
    private String providerPayDate;

    private BigDecimal amount;

    /** true = hoan toan phan (02), false = hoan mot phan (03). */
    private boolean fullRefund;

    private String orderInfo;
    private String createBy;
    private String clientIp;
}
