package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TaoDonTaiQuayRequest {

    private List<TaoDonTaiQuayRequest.ItemRequest> items;
    private Integer idKhachHang;
    private String maPhieuGiamGia;
    private Integer idPhuongThucThanhToan;
    private BigDecimal soTienKhachDua;
    private String maGiaoDich;
    private String ghiChu;
    private Integer idHoaDonCho;


    private List<ThanhToanItemRequest> danhSachThanhToan;

    @Getter
    @Setter
    public static class ThanhToanItemRequest {
        private Integer idPhuongThucThanhToan;
        private BigDecimal soTien;
        private BigDecimal soTienKhachDua;
        private String maGiaoDich;
    }

    @Getter
    @Setter
    public static class ItemRequest {
        private Integer idChiTietSanPham;
        private Integer soLuong;
    }
}
