package org.example.templatejava6.staff.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.common.entity.NhanVien;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class NhanVienResponse {

    private Integer id;
    private String maNhanVien;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String maVaiTro;
    private String tenVaiTro;
    private Boolean trangThai;
    private String gioiTinh;
    private LocalDate ngayVaoLam;

    public NhanVienResponse(NhanVien nv) {
        this.id = nv.getId();
        this.maNhanVien = nv.getMaNhanVien();
        this.hoTen = nv.getHoTen();
        this.email = nv.getEmail();
        this.soDienThoai = nv.getSoDienThoai();
        if (nv.getVaiTro() != null) {
            this.maVaiTro = nv.getVaiTro().getMaVaiTro();
            this.tenVaiTro = nv.getVaiTro().getTenVaiTro();
        }
        this.trangThai = nv.getTrangThai();
        this.gioiTinh = nv.getGioiTinh();
        this.ngayVaoLam = nv.getNgayVaoLam();
    }
}
