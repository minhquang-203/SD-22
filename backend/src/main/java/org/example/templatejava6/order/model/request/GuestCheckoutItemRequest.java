package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Một dòng sản phẩm khách chưa đăng nhập chọn mua (giỏ hàng nằm ở localStorage phía client). */
@Getter
@Setter
public class GuestCheckoutItemRequest {

    @NotNull(message = "Thiếu sản phẩm trong giỏ hàng")
    private Integer idChiTietSanPham;

    @NotNull(message = "Thiếu số lượng sản phẩm")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
}
