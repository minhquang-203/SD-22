package org.example.templatejava6.category.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.category.entity.DangSanPham;

@Getter
@Setter
public class DangSanPhamResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String moTa;

    public DangSanPhamResponse(DangSanPham d) {
        this.id = d.getId();
        this.ma = d.getMa();
        this.ten = d.getTen();
        this.moTa = d.getMoTa();
    }
}
