package org.example.templatejava6.shipping.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateShippingOrderResponse {
    /** Ma van don GHN. */
    private String orderCode;
    /** Tong phi van chuyen. */
    private Long totalFee;
    /** Thoi gian du kien giao. */
    private String expectedDeliveryTime;
}
