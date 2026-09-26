package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PosTinhGiaResponse {

    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal thanhTien;
    private String maPhieuGiamGia;
    /** Đơn giá hiện tại từng biến thể (để POS đồng bộ khi giá đổi). */
    private java.util.List<PosDongGia> dongGias;

    @Getter
    @Setter
    public static class PosDongGia {
        private Integer idChiTietSanPham;
        private BigDecimal donGia;
        private BigDecimal giaGoc;
        private Integer soLuongTon;
        private Boolean trangThai;
    }
}
