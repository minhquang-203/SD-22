package org.example.templatejava6.voucher.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.voucher.entity.ChiTietDotGiamGia;

import java.math.BigDecimal;

@Getter
@Setter
public class ChiTietDotGiamGiaResponse {

    private Integer id;
    private Integer idDotGiamGia;
    private String maDotGiamGia;
    private String tenDotGiamGia;
    private BigDecimal phanTramGiam;
    private Integer idChiTietSanPham;
    private String sku;
    private String tenSanPham;
    private BigDecimal giaBan;
    private BigDecimal giaSauGiam;

    public ChiTietDotGiamGiaResponse(ChiTietDotGiamGia ct) {
        this.id = ct.getId();
        if (ct.getIdDotGiamGia() != null) {
            this.idDotGiamGia = ct.getIdDotGiamGia().getId();
            this.maDotGiamGia = ct.getIdDotGiamGia().getMa();
            this.tenDotGiamGia = ct.getIdDotGiamGia().getTen();
            this.phanTramGiam = ct.getIdDotGiamGia().getPhanTramGiam();
        }
        ChiTietSanPham ctsp = ct.getIdChiTietSanPham();
        if (ctsp != null) {
            this.idChiTietSanPham = ctsp.getId();
            this.sku = ctsp.getSku();
            this.giaBan = ctsp.getGiaBan();
            if (ctsp.getSanPham() != null) {
                this.tenSanPham = ctsp.getSanPham().getTen();
            }
        }
        this.giaSauGiam = ct.getGiaSauGiam();
    }
}
