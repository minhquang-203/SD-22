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
}
