package org.example.templatejava6.voucher.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DotGiamGiaRequest {

    @NotBlank(message = "Mã đợt giảm giá không được để trống")
    private String ma;

    @NotBlank(message = "Tên đợt giảm giá không được để trống")
    private String ten;

    @NotNull(message = "Phần trăm giảm không được để trống")
    @Positive(message = "Phần trăm giảm phải lớn hơn 0")
    private BigDecimal phanTramGiam;

    private LocalDateTime ngayBatDau;

    private LocalDateTime ngayKetThuc;
}
