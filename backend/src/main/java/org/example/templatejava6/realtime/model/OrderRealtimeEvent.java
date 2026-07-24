package org.example.templatejava6.realtime.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderRealtimeEvent {

    public static final String TYPE_CREATED = "ORDER_CREATED";
    public static final String TYPE_STATUS_CHANGED = "ORDER_STATUS_CHANGED";

    private final String type;
    private final Integer idHoaDon;
    private final String maHoaDon;
    private final Integer idKhachHang;
    private final String trangThai;
    private final String trangThaiLabel;
    private final String trangThaiCu;
    private final String loaiDon;
    private final String message;
}
