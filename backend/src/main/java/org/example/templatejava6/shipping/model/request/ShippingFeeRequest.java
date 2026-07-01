package org.example.templatejava6.shipping.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingFeeRequest {

    @NotNull(message = "Vui lòng chọn quận/huyện nhận hàng")
    private Integer toDistrictId;

    @NotNull(message = "Vui lòng chọn phường/xã nhận hàng")
    private String toWardCode;

    /** Khoi luong don hang (gram). Neu trong se dung mac dinh trong cau hinh. */
    private Integer weight;

    /** Gia tri khai gia (insurance), thuong la tong tien hang. */
    private Long insuranceValue;
}
