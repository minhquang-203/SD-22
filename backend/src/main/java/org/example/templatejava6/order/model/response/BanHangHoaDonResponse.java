package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BanHangHoaDonResponse {

    private Integer id;
    private String maHoaDon;
    private String loaiDon;
    private String trangThai;
    private LocalDateTime ngayTao;
    private String tenKhachHang;
    private String tenNhanVien;
    private String tenPhuongThucThanhToan;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal thanhTien;
    private BigDecimal soTienKhachDua;
    private BigDecimal tienThua;
    private String ghiChu;
    private String paymentUrl;
    private String transactionRef;
    private String trangThaiThanhToan;
    private List<BanHangChiTietResponse> items;

    public static BanHangHoaDonResponse from(
            HoaDon hd,
            ThanhToanHoaDon tt,
            List<BanHangChiTietResponse> lines) {
        BanHangHoaDonResponse res = new BanHangHoaDonResponse();
        res.setId(hd.getId());
        res.setMaHoaDon(hd.getMaHoaDon());
        res.setLoaiDon(hd.getLoaiDon());
        res.setTrangThai(hd.getTrangThai() != null ? hd.getTrangThai().name() : null);
        res.setNgayTao(hd.getNgayTao());
        res.setTenKhachHang(hd.getIdKhachHang() != null ? hd.getIdKhachHang().getHoTen() : "Khách lẻ");
        res.setTenNhanVien(hd.getIdNhanVien() != null ? hd.getIdNhanVien().getHoTen() : null);
        res.setTenPhuongThucThanhToan(
                hd.getIdPhuongThucThanhToan() != null ? hd.getIdPhuongThucThanhToan().getTen() : null);
        res.setTongTien(hd.getTongTien());
        res.setTienGiamGia(hd.getTienGiamGia());
        res.setThanhTien(hd.getThanhTien());
        res.setGhiChu(hd.getGhiChu());
        if (tt != null) {
            res.setSoTienKhachDua(tt.getSoTienKhachDua());
            res.setTienThua(tt.getTienThua());
        }
        res.setItems(lines);
        return res;
    }

    @Getter
    @Setter
    public static class BanHangChiTietResponse {
        private String sku;
        private String tenSanPham;
        private String bienThe;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;

        public BanHangChiTietResponse(HoaDonChiTiet line) {
            var cts = line.getIdChiTietSanPham();
            this.sku = cts.getSku();
            this.tenSanPham = cts.getSanPham() != null ? cts.getSanPham().getTen() : null;
            String dt = cts.getDungTichMl() != null
                    ? cts.getDungTichMl().stripTrailingZeros().toPlainString() + "ml" : null;
            String ms = cts.getMauSac() != null ? cts.getMauSac().getTen() : null;
            if (dt != null && ms != null) {
                this.bienThe = dt + " / " + ms;
            } else if (dt != null) {
                this.bienThe = dt;
            } else {
                this.bienThe = ms;
            }
            this.soLuong = line.getSoLuong();
            this.donGia = line.getDonGia();
            this.thanhTien = line.getThanhTien();
        }
    }
}
