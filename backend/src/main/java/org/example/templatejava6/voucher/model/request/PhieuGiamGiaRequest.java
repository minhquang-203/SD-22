package org.example.templatejava6.voucher.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaRequest {

    @NotBlank(message = "Mã phiếu giảm giá không được để trống")
    private String ma;

    private String ten;

    @NotNull(message = "Loại phiếu giảm giá không được để trống")
    private LoaiPhieuGiamGia loai;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTri;

    private BigDecimal giaTriDonToiThieu;

    private BigDecimal giamToiDa;

    private Integer soLuong;

    private LocalDateTime ngayBatDau;

    private LocalDateTime ngayKetThuc;
}
