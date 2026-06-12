package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DonChoDetailResponse {

    private Integer id;
    private Integer idKhachHang;
    private String hoTenKhachHang;
    private String soDienThoai;
    private List<DonChoLineResponse> items;

    @Getter
    @Setter
    public static class DonChoLineResponse {
        private Integer idChiTietSanPham;
        private String sku;
        private String tenSanPham;
        private BigDecimal dungTichMl;
        private String tenMauSac;
        private BigDecimal donGia;
        private Integer soLuong;
        private Integer soLuongTon;
    }
}
