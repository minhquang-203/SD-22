package org.example.templatejava6.payment.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KetQuaThanhToanResponse {

    private boolean success;
    private String provider;
    private Integer idHoaDon;
    private String maHoaDon;
    private String transactionRef;
    private String providerTransactionNo;
    private String responseCode;
    private String message;
    /** Chỉ có với đơn khách vãng lai đã thanh toán thành công — để hiện nút theo dõi đơn. */
    private String trackingToken;
}
