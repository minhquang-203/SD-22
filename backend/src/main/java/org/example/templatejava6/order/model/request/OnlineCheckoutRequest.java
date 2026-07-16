package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnlineCheckoutRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm trong giỏ hàng")
    private List<@NotNull Integer> idsChiTietGioHang;

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String maPhuongThucThanhToan;

    private String maPhieuGiamGia;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String diaChiGiao;

    private String ghiChu;

    /** Ten nguoi nhan thuc te (de tao van don GHN). Neu trong se lay theo tai khoan. */
    private String tenNguoiNhan;

    /** So dien thoai nguoi nhan thuc te. Neu trong se lay theo tai khoan. */
    private String sdtNguoiNhan;

    /** Ma quan/huyen GHN cua nguoi nhan (to_district_id), dung de tinh phi + tao van don. */
    @NotNull(message = "Vui lòng chọn quận/huyện giao hàng")
    private Integer toDistrictId;

    /** Ma phuong/xa GHN cua nguoi nhan (to_ward_code), dung de tinh phi + tao van don. */
    @NotBlank(message = "Vui lòng chọn phường/xã giao hàng")
    private String toWardCode;
}
