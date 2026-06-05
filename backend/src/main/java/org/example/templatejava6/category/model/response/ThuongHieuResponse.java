package org.example.templatejava6.category.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.category.entity.ThuongHieu;

@Getter
@Setter
public class ThuongHieuResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String xuatXu;
    private Boolean trangThai;

    public ThuongHieuResponse(ThuongHieu th) {
        this.id = th.getId();
        this.ma = th.getMa();
        this.ten = th.getTen();
        this.xuatXu = th.getXuatXu();
        this.trangThai = th.getTrangThai();
    }
}
