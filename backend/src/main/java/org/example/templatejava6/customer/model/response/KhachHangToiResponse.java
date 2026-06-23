package org.example.templatejava6.customer.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;

@Getter
@Setter
public class KhachHangToiResponse {

    private Integer id;
    private String hoTen;
    private String email;
    private String soDienThoai;

    public KhachHangToiResponse(KhachHang kh) {
        this.id = kh.getId();
        this.hoTen = kh.getHoTen();
        this.email = kh.getEmail();
        this.soDienThoai = kh.getSoDienThoai();
    }
}
