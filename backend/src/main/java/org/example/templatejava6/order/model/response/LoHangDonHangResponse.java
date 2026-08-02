package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoHangDonHangResponse {

    private Integer idLoHang;
    private String soLo;
    private LocalDate hanSuDung;
    private LocalDate ngayNhap;
    private Integer soLuongDaBan;
    private Integer idChiTietSanPham;
    private String sku;
    private String tenSanPham;
}
