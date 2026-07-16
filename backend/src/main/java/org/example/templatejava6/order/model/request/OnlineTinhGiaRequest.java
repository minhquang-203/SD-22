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

    /** Ma quan/huyen GHN — server dung de tinh phi van chuyen. */
    private Integer toDistrictId;

    /** Ma phuong/xa GHN — server dung de tinh phi van chuyen. */
    private String toWardCode;
}
