package org.example.templatejava6.support.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.support.entity.PhienHoTro;

import java.time.LocalDateTime;

@Getter
@Setter
public class PhienHoTroResponse {
    private Integer id;
    private Integer idKhachHang;
    private String tenKhachHang;
    private String trangThai;
    private Integer nguoiXuLyId;
    private String tenNguoiXuLy;
    private LocalDateTime ngayTao;
    private LocalDateTime capNhatCuoi;
    private String tinCuoi;
    private String nguoiGuiCuoi;
    /** Số tin khách gửi chưa đọc (da_doc = false). */
    private Long soTinChuaDoc;

    public static PhienHoTroResponse from(PhienHoTro phien) {
        PhienHoTroResponse res = new PhienHoTroResponse();
        res.setId(phien.getId());
        if (phien.getIdKhachHang() != null) {
            res.setIdKhachHang(phien.getIdKhachHang().getId());
            res.setTenKhachHang(phien.getIdKhachHang().getHoTen());
        } else {
            res.setTenKhachHang("Khách vãng lai");
        }
        res.setTrangThai(phien.getTrangThai());
        if (phien.getNguoiXuLy() != null) {
            res.setNguoiXuLyId(phien.getNguoiXuLy().getId());
            res.setTenNguoiXuLy(phien.getNguoiXuLy().getHoTen());
        }
        res.setNgayTao(phien.getNgayTao());
        res.setCapNhatCuoi(phien.getCapNhatCuoi());
        return res;
    }
}
