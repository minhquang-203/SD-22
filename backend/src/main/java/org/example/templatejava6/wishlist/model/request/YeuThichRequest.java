package org.example.templatejava6.wishlist.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YeuThichRequest {

    @NotNull(message = "Id khách hàng không được để trống")
    private Integer idKhachHang;

    @NotNull(message = "Id sản phẩm không được để trống")
    private Integer idSanPham;
}
