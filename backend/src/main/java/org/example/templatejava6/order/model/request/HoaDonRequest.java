package org.example.templatejava6.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonRequest {

    private Integer id;

    @NotBlank(message = "Mã hóa đơn không được để trống")
    private String maHoaDon;

    private Integer idKhachHang;
    private Integer idNhanVien;
    private Integer idPhuongThucThanhToan;
    private Integer idPhieuGiamGia;
    private String loaiDon;
    private String diaChiGiao;
    private BigDecimal phiVanChuyen;
    private String ghiChu;

    @Valid
    private List<HoaDonChiTietRequest> chiTiets;
}
