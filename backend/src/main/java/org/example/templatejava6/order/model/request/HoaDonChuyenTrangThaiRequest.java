package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.common.enums.TrangThaiDonHang;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonChuyenTrangThaiRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private TrangThaiDonHang trangThai;

    private String ghiChu;

    private Integer idNhanVien;
}
