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
        /** Lô đã chọn khi giữ đơn (1 lô / dòng); null = FEFO hoặc xem {@link #phanBoLos}. */
        private Integer idLoHang;
        private String soLo;
        /** Phân bổ nhiều lô trên 1 dòng (giữ đơn / nạp lại POS). */
        private List<PhanBoLoItem> phanBoLos;
    }

    @Getter
    @Setter
    public static class PhanBoLoItem {
        private Integer idLoHang;
        private String soLo;
        private Integer soLuong;
        private java.time.LocalDate hanSuDung;
    }
}
