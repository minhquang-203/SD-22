package org.example.templatejava6.payment.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.PhuongThucThanhToan;

@Getter
@Setter
public class PhuongThucThanhToanResponse {

    private Integer id;
    private String ma;
    private String ten;

    public PhuongThucThanhToanResponse(PhuongThucThanhToan pttt) {
        this.id = pttt.getId();
        this.ma = pttt.getMa();
        this.ten = pttt.getTen();
    }
}
