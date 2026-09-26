package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiaHienTaiRequest {

    @NotEmpty(message = "Danh sách biến thể trống")
    private List<@NotNull Integer> idsChiTietSanPham;
}
