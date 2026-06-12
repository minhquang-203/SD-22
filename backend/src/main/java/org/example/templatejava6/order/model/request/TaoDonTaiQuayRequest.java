package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TaoDonTaiQuayRequest {

    private List<ItemRequest> items;
    private Integer idKhachHang;
    private Integer idNhanVien;
    private String maPhieuGiamGia;
    private Integer idPhuongThucThanhToan;
    private BigDecimal soTienKhachDua;
    private String maGiaoDich;
    private String ghiChu;
    private Integer idHoaDonCho;

    @Getter
    @Setter
    public static class ItemRequest {
        private Integer idChiTietSanPham;
        private Integer soLuong;
    }
}
