package org.example.templatejava6.cart.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThemGioHangRequest {

    @NotNull(message = "Id khách hàng không được để trống")
    private Integer idKhachHang;

    @NotNull(message = "Id chi tiết sản phẩm không được để trống")
    private Integer idChiTietSanPham;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
}
