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

    /** ID phường/xã đơn vị hành chính mới (GHN v3). */
    private Integer toWardIdV2;

    /** Địa chỉ text để tính phí với địa chỉ mới. */
    private String toAddressV2;

    /** Legacy 3 cấp. */
    private Integer toDistrictId;

    /** Legacy / hoặc String.valueOf(toWardIdV2). */
    private String toWardCode;
}
