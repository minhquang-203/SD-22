package org.example.templatejava6.shipping.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingFeeRequest {

    /**
     * ID phường/xã đơn vị hành chính mới (GHN v3 {@code _id}).
     * Dùng với {@code is_new_to_address=true}.
     */
    private Integer toWardIdV2;

    /** Địa chỉ text nhận hàng (bắt buộc khi tính phí bằng địa chỉ mới). */
    private String toAddressV2;

    /** Legacy 3 cấp — chỉ dùng khi không có toWardIdV2. */
    private Integer toDistrictId;

    /** Legacy ward code — chỉ dùng khi không có toWardIdV2. */
    private String toWardCode;

    /** Khoi luong don hang (gram). Neu trong se dung mac dinh trong cau hinh. */
    private Integer weight;

    /** Gia tri khai gia (insurance), thuong la tong tien hang. */
    private Long insuranceValue;
}
