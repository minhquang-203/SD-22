package org.example.templatejava6.realtime.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminNotificationEvent {

    public static final String TYPE_NOTIFICATION = "ADMIN_NOTIFICATION";

    private final String type;
    private final Integer id;
    private final String loai;
    private final String tieuDe;
    private final String noiDung;
    private final String link;
    private final Integer idThamChieu;
    private final String maThamChieu;
}
