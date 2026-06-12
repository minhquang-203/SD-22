package org.example.templatejava6.customer.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class KhachHangResponse {

    private Integer id;
    private String maKhachHang;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String gioiTinh;
    private LocalDate ngaySinh;
    private Integer diemTichLuy;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private String tenLoaiDa;

    public KhachHangResponse(KhachHang kh) {
        this.id = kh.getId();
        this.maKhachHang = kh.getMaKhachHang();
        this.hoTen = kh.getHoTen();
        this.email = kh.getEmail();
        this.soDienThoai = kh.getSoDienThoai();
        this.gioiTinh = kh.getGioiTinh();
        this.ngaySinh = kh.getNgaySinh();
        this.diemTichLuy = kh.getDiemTichLuy();
        this.trangThai = kh.getTrangThai();
        this.ngayTao = kh.getNgayTao();
        this.tenLoaiDa = kh.getLoaiDa() != null ? kh.getLoaiDa().getTen() : null;
    }
}
