package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GiuDonChoResponse {

    private Integer id;
    private String maHoaDon;

    public GiuDonChoResponse(Integer id, String maHoaDon) {
        this.id = id;
        this.maHoaDon = maHoaDon;
    }
}
