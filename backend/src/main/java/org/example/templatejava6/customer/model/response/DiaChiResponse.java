package org.example.templatejava6.customer.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.customer.entity.DiaChiKhachHang;

@Getter
@Setter
public class DiaChiResponse {

    private String hoTenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String diaChiChiTiet;
    private Boolean macDinh;

    public DiaChiResponse(DiaChiKhachHang dc) {
        this.hoTenNguoiNhan = dc.getHoTenNguoiNhan();
        this.soDienThoai = dc.getSoDienThoai();
        this.tinhThanh = dc.getTinhThanh();
        this.quanHuyen = dc.getQuanHuyen();
        this.phuongXa = dc.getPhuongXa();
        this.diaChiChiTiet = dc.getDiaChiChiTiet();
        this.macDinh = dc.getMacDinh();
    }
}
