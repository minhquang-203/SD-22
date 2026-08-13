package org.example.templatejava6.realtime.model;

import lombok.Builder;
import lombok.Getter;

/** Payload realtime cho chuông thông báo phía khách hàng. */
@Getter
@Builder
public class CustomerNotificationEvent {

    public static final String TYPE_NOTIFICATION = "CUSTOMER_NOTIFICATION";

    private final String type;
    private final Integer id;
    private final Integer idKhachHang;
    private final String loai;
    private final String tieuDe;
    private final String noiDung;
    private final String link;
    private final Integer idThamChieu;
    private final String maThamChieu;
}
