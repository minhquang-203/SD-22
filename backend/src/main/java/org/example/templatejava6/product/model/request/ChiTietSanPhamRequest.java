package org.example.templatejava6.product.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSanPhamRequest {

    private Integer id;

    private Integer idSanPham;

    private Integer idMauSac;

    @NotBlank(message = "SKU không được để trống")
    private String sku;

    private BigDecimal dungTichMl;

    @NotNull(message = "Giá bán không được để trống")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal giaBan;

    private Integer soLuongTon;
    private Boolean trangThai;
}
