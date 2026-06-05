package org.example.templatejava6.category.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.category.entity.DanhMuc;

@Getter
@Setter
public class DanhMucResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean trangThai;

    public DanhMucResponse(DanhMuc dm) {
        this.id = dm.getId();
        this.ma = dm.getMa();
        this.ten = dm.getTen();
        this.moTa = dm.getMoTa();
        this.trangThai = dm.getTrangThai();
    }
}
