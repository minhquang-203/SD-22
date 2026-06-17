package org.example.templatejava6.product.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoHangRequest {

    @NotNull(message = "Biến thể không được để trống")
    private Integer idChiTietSanPham;

    @NotBlank(message = "Số lô không được để trống")
    private String soLo;

    @NotNull(message = "Ngày nhập không được để trống")
    private LocalDate ngayNhap;

    private LocalDate hanSuDung;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Positive(message = "Số lượng nhập phải lớn hơn 0")
    private Integer soLuongNhap;

    private String ghiChu;
}
