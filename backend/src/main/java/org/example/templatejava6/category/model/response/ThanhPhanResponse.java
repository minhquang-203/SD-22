package org.example.templatejava6.category.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.category.entity.ThanhPhan;

@Getter
@Setter
public class ThanhPhanResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String loai;
    private String moTa;

    public ThanhPhanResponse(ThanhPhan t) {
        this.id = t.getId();
        this.ma = t.getMa();
        this.ten = t.getTen();
        this.loai = t.getLoai();
        this.moTa = t.getMoTa();
    }
}
