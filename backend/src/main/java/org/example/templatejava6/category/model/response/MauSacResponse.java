package org.example.templatejava6.category.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.category.entity.MauSac;

@Getter
@Setter
public class MauSacResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String maHex;

    public MauSacResponse(MauSac m) {
        this.id = m.getId();
        this.ma = m.getMa();
        this.ten = m.getTen();
        this.maHex = m.getMaHex();
    }
}
