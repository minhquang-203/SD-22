package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiuDonChoRequest {

    private List<TaoDonTaiQuayRequest.ItemRequest> items;
    private Integer idKhachHang;
    /** Tên khách vãng lai (khi chưa gắn thành viên) — lưu kèm đơn chờ */
    private String tenKhachHang;
    /** SĐT khách vãng lai */
    private String soDienThoai;
    private String ghiChu;
}
