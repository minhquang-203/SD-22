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

    /** Id khách lấy từ JWT — không nhận từ client. */
    @NotNull(message = "Id sản phẩm không được để trống")
    private Integer idSanPham;
}
