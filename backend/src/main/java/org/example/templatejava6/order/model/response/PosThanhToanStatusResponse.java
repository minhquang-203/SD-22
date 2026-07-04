package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosThanhToanStatusResponse {

    private Integer idHoaDon;
    private String maHoaDon;
    private String trangThaiThanhToan;
    private BanHangHoaDonResponse hoaDon;

    public static PosThanhToanStatusResponse of(
            Integer idHoaDon,
            String maHoaDon,
            String trangThaiThanhToan,
            BanHangHoaDonResponse hoaDon) {
        PosThanhToanStatusResponse res = new PosThanhToanStatusResponse();
        res.setIdHoaDon(idHoaDon);
        res.setMaHoaDon(maHoaDon);
        res.setTrangThaiThanhToan(trangThaiThanhToan);
        res.setHoaDon(hoaDon);
        return res;
    }
}
