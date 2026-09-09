package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PosTinhGiaRequest {

    private List<TaoDonTaiQuayRequest.ItemRequest> items;
    private String maPhieuGiamGia;
    /** Khách đang chọn trên POS — cần để áp voucher cá nhân (CA_NHAN). */
    private Integer idKhachHang;
}
