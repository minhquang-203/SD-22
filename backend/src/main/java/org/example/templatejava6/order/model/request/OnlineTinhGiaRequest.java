package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnlineTinhGiaRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm trong giỏ hàng")
    private List<@NotNull Integer> idsChiTietGioHang;

    private String maPhieuGiamGia;

    /** Tên tỉnh/thành — địa chỉ 2 cấp. */
    private String toProvinceName;

    /** Tên phường/xã — địa chỉ 2 cấp. */
    private String toWardName;

    /** Địa chỉ cụ thể (số nhà, đường). */
    private String toAddressV2;

    /** Legacy 3 cấp. */
    private Integer toDistrictId;

    /** Legacy ward code. */
    private String toWardCode;
}
