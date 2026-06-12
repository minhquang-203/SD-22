package org.example.templatejava6.staff.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.NhanVien;

@Getter
@Setter
public class NhanVienResponse {

    private Integer id;
    private String maNhanVien;
    private String hoTen;

    public NhanVienResponse(NhanVien nv) {
        this.id = nv.getId();
        this.maNhanVien = nv.getMaNhanVien();
        this.hoTen = nv.getHoTen();
    }
}
