package org.example.templatejava6.shipping.model.response;

import lombok.Getter;

@Getter
public class ShippingFeeResponse {

    /** Tong phi van chuyen (VND). */
    private final long total;

    /** True neu phi lay truc tiep tu GHN, false neu dung phi mac dinh. */
    private final boolean fromGhn;

    /** Thong bao bo sung (vd: ly do dung phi mac dinh). */
    private final String message;

    private ShippingFeeResponse(long total, boolean fromGhn, String message) {
        this.total = total;
        this.fromGhn = fromGhn;
        this.message = message;
    }

    public static ShippingFeeResponse fromGhn(long total) {
        return new ShippingFeeResponse(total, true, null);
    }

    public static ShippingFeeResponse fallback(long total, String message) {
        return new ShippingFeeResponse(total, false, message);
    }
}
