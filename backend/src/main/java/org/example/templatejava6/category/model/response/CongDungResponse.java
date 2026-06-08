package org.example.templatejava6.category.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.category.entity.CongDung;

@Getter
@Setter
public class CongDungResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean trangThai;

    public CongDungResponse(CongDung c) {
        this.id = c.getId();
        this.ma = c.getMa();
        this.ten = c.getTen();
        this.moTa = c.getMoTa();
        this.trangThai = c.getTrangThai();
    }
}
