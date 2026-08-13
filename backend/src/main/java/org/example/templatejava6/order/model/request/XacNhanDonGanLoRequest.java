package org.example.templatejava6.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class XacNhanDonGanLoRequest {

    @NotEmpty(message = "Danh sách dòng hàng không được để trống")
    @Valid
    private List<DongHangGanLo> dongHang;

    private String ghiChu;

    private Integer idNhanVien;

    @Getter
    @Setter
    public static class DongHangGanLo {
        @NotNull(message = "Thiếu id dòng hóa đơn")
        private Integer idHoaDonChiTiet;

        @NotEmpty(message = "Phân bổ lô không được để trống")
        @Valid
        private List<PhanBoLoItem> phanBoLo;
    }

    @Getter
    @Setter
    public static class PhanBoLoItem {
        @NotNull(message = "Thiếu id lô")
        private Integer idLoHang;

        @NotNull(message = "Thiếu số lượng lô")
        private Integer soLuong;
    }
}
