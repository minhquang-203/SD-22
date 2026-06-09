package org.example.templatejava6.product.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.AnhSanPham;

@Getter
@Setter
public class AnhSanPhamResponse {

    private Integer id;
    private String url;
    private Boolean laAnhChinh;
    private Integer thuTu;
    private Integer idChiTietSanPham;

    public AnhSanPhamResponse(AnhSanPham anh) {
        this.id = anh.getId();
        this.url = anh.getUrl();
        this.laAnhChinh = anh.getLaAnhChinh();
        this.thuTu = anh.getThuTu();
        this.idChiTietSanPham = anh.getChiTietSanPham() != null ? anh.getChiTietSanPham().getId() : null;
    }
}
