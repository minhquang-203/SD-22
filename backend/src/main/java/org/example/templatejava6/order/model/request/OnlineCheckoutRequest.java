package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OnlineCheckoutRequest {

    @NotNull(message = "Vui lòng chọn khách hàng")
    private Integer idKhachHang;

    @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm trong giỏ hàng")
    private List<@NotNull Integer> idsChiTietGioHang;

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String maPhuongThucThanhToan;

    private String maPhieuGiamGia;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String diaChiGiao;

    @PositiveOrZero(message = "Phí vận chuyển không hợp lệ")
    private BigDecimal phiVanChuyen;

    private String ghiChu;

    /** Ten nguoi nhan thuc te (de tao van don GHN). Neu trong se lay theo tai khoan. */
    private String tenNguoiNhan;

    /** So dien thoai nguoi nhan thuc te. Neu trong se lay theo tai khoan. */
    private String sdtNguoiNhan;

    /** Ma quan/huyen GHN cua nguoi nhan (to_district_id), dung de tao van don GHN. */
    private Integer toDistrictId;

    /** Ma phuong/xa GHN cua nguoi nhan (to_ward_code), dung de tao van don GHN. */
    private String toWardCode;
}
