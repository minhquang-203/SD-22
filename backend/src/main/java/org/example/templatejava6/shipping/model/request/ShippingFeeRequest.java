package org.example.templatejava6.shipping.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingFeeRequest {

    /** Tên tỉnh/thành — địa chỉ 2 cấp (GHN {@code to_province_name}). */
    private String toProvinceName;

    /** Tên phường/xã — địa chỉ 2 cấp (GHN {@code to_ward_name}). */
    private String toWardName;

    /** Địa chỉ cụ thể (số nhà, đường). */
    private String toAddressV2;

    /** Legacy 3 cấp — chỉ dùng khi thiếu tên tỉnh/phường. */
    private Integer toDistrictId;

    /** Legacy ward code — chỉ dùng khi thiếu tên tỉnh/phường. */
    private String toWardCode;

    /** Khoi luong don hang (gram). Neu trong se dung mac dinh trong cau hinh. */
    private Integer weight;

    /** Gia tri khai gia (insurance), thuong la tong tien hang. */
    private Long insuranceValue;
}
