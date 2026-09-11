package org.example.templatejava6.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Tính tạm giá + phí vận chuyển cho khách chưa đăng nhập. Không áp dụng voucher. */
@Getter
@Setter
public class GuestTinhGiaRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm")
    private List<@Valid GuestCheckoutItemRequest> items;

    /** Tên tỉnh/thành — địa chỉ 2 cấp. */
    private String toProvinceName;

    /** Province _id GHN v3. */
    private Integer toProvinceId;

    /** Tên phường/xã — địa chỉ 2 cấp. */
    private String toWardName;

    /** Địa chỉ cụ thể (số nhà, đường). */
    private String toAddressV2;

    /** Legacy 3 cấp. */
    private Integer toDistrictId;

    /** Legacy ward code. */
    private String toWardCode;
}
