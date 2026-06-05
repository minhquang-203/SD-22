package org.example.templatejava6.wishlist.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.model.response.SanPhamResponse;
import org.example.templatejava6.wishlist.entity.SanPhamYeuThich;

import java.time.LocalDateTime;

@Getter
@Setter
public class YeuThichResponse {

    private Integer id;
    private Integer idKhachHang;
    private Integer idSanPham;
    private LocalDateTime ngayThem;
    private SanPhamResponse sanPham;

    public YeuThichResponse(SanPhamYeuThich yt) {
        this.id = yt.getId();
        this.idKhachHang = yt.getIdKhachHang();
        this.idSanPham = yt.getSanPham() != null ? yt.getSanPham().getId() : null;
        this.ngayThem = yt.getNgayThem();
        if (yt.getSanPham() != null) {
            this.sanPham = new SanPhamResponse(yt.getSanPham());
        }
    }
}
