package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiuDonChoRequest {

    private List<TaoDonTaiQuayRequest.ItemRequest> items;
    private Integer idKhachHang;
    private String ghiChu;
}
