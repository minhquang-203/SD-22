package org.example.templatejava6.payment.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaoThanhToanResponse {

    private String provider;
    private Integer idHoaDon;
    private String maHoaDon;
    private String transactionRef;
    private String paymentUrl;
}
