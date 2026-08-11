package org.example.templatejava6.shipping.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Yeu cau tao van don hoan tra hang: nguoi gui la khach hang (from_*),
 * nguoi nhan la shop (lay tu cau hinh GHN).
 */
@Getter
@Setter
public class ReturnShippingOrderRequest {

    private String fromName;
    private String fromPhone;
    private String fromAddress;
    private Integer fromDistrictId;
    private String fromWardCode;
    /** Tên tỉnh (địa chỉ mới 2 cấp). */
    private String fromProvinceName;
    /** Tên phường/xã (địa chỉ mới 2 cấp). */
    private String fromWardName;

    private Long insuranceValue;
    private Integer weight;

    /** Ca lay hang GHN (pick_shift), lay tu API danh sach ca lay. */
    private Integer pickShiftId;

    private List<CreateShippingOrderRequest.Item> items;
}
