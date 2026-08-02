package org.example.templatejava6.realtime.model;

import lombok.Builder;
import lombok.Getter;
import org.example.templatejava6.support.model.response.TinNhanHoTroResponse;

@Getter
@Builder
public class HoTroRealtimeEvent {
    public static final String TYPE_TIN_NHAN_MOI = "TIN_NHAN_MOI";

    private String type;
    private Integer idPhien;
    private String tenKhachHang;
    private String noiDungTomTat;
    private TinNhanHoTroResponse tinNhan;
}
