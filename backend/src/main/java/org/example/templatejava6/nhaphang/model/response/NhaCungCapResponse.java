package org.example.templatejava6.nhaphang.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.nhaphang.entity.NhaCungCap;

@Getter
@Setter
public class NhaCungCapResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String ghiChu;
    private Boolean trangThai;

    public NhaCungCapResponse(NhaCungCap n) {
        this.id = n.getId();
        this.ma = n.getMa();
        this.ten = n.getTen();
        this.soDienThoai = n.getSoDienThoai();
        this.email = n.getEmail();
        this.diaChi = n.getDiaChi();
        this.ghiChu = n.getGhiChu();
        this.trangThai = n.getTrangThai();
    }
}
