package org.example.templatejava6.cart.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.cart.entity.ChiTietGioHang;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.voucher.model.response.VariantSaleInfo;

import java.math.BigDecimal;

@Getter
@Setter
public class ChiTietGioHangResponse {

    private Integer id;
    private Integer idGioHang;
    private Integer idChiTietSanPham;
    private Integer idSanPham;
    private String tenSanPham;
    private String tenThuongHieu;
    private String sku;
    private BigDecimal dungTichMl;
    private String tenMauSac;
    private String anhUrl;
    private BigDecimal giaBan;
    private BigDecimal giaGoc;
    private BigDecimal phanTramGiam;
    private Integer soLuongTon;
    private Integer soLuong;
    private BigDecimal thanhTien;
    private Boolean trangThaiBienThe;
    private Boolean trangThaiSanPham;

    public ChiTietGioHangResponse(ChiTietGioHang chiTietGioHang) {
        this(chiTietGioHang, null);
    }

    public ChiTietGioHangResponse(ChiTietGioHang chiTietGioHang, String anhUrl) {
        ChiTietSanPham chiTietSanPham = chiTietGioHang.getChiTietSanPham();
        SanPham sanPham = chiTietSanPham != null ? chiTietSanPham.getSanPham() : null;

        this.id = chiTietGioHang.getId();
        this.idGioHang = chiTietGioHang.getGioHang() != null ? chiTietGioHang.getGioHang().getId() : null;
        this.idChiTietSanPham = chiTietSanPham != null ? chiTietSanPham.getId() : null;
        this.idSanPham = sanPham != null ? sanPham.getId() : null;
        this.tenSanPham = sanPham != null ? sanPham.getTen() : null;
        this.tenThuongHieu = sanPham != null && sanPham.getThuongHieu() != null
                ? sanPham.getThuongHieu().getTen()
                : null;
        this.sku = chiTietSanPham != null ? chiTietSanPham.getSku() : null;
        this.dungTichMl = chiTietSanPham != null ? chiTietSanPham.getDungTichMl() : null;
        this.tenMauSac = chiTietSanPham != null && chiTietSanPham.getMauSac() != null
                ? chiTietSanPham.getMauSac().getTen()
                : null;
        this.anhUrl = anhUrl;
        this.giaBan = chiTietSanPham != null ? chiTietSanPham.getGiaBan() : BigDecimal.ZERO;
        this.giaGoc = this.giaBan;
        this.soLuongTon = chiTietSanPham != null ? chiTietSanPham.getSoLuongTon() : 0;
        this.soLuong = chiTietGioHang.getSoLuong();
        this.thanhTien = this.giaBan.multiply(BigDecimal.valueOf(this.soLuong != null ? this.soLuong : 0L));
        this.trangThaiBienThe = chiTietSanPham != null ? chiTietSanPham.getTrangThai() : null;
        this.trangThaiSanPham = sanPham != null ? sanPham.getTrangThai() : null;
    }

    public void applySalePrice(VariantSaleInfo sale) {
        if (sale == null || sale.getGiaSauGiam() == null) {
            return;
        }
        this.giaGoc = sale.getGiaGoc() != null ? sale.getGiaGoc() : this.giaBan;
        this.giaBan = sale.getGiaSauGiam();
        this.phanTramGiam = sale.getPhanTramGiam();
        this.thanhTien = this.giaBan.multiply(BigDecimal.valueOf(this.soLuong != null ? this.soLuong : 0L));
    }
}
