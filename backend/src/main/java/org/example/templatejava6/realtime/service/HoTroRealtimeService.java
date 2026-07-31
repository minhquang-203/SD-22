package org.example.templatejava6.realtime.service;

import org.example.templatejava6.realtime.event.HoTroRealtimeAppEvent;
import org.example.templatejava6.realtime.model.HoTroRealtimeEvent;
import org.example.templatejava6.support.entity.PhienHoTro;
import org.example.templatejava6.support.entity.TinNhanHoTro;
import org.example.templatejava6.support.model.response.TinNhanHoTroResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class HoTroRealtimeService {

    private final ApplicationEventPublisher eventPublisher;

    public HoTroRealtimeService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishTinNhanMoi(PhienHoTro phien, TinNhanHoTro tin) {
        if (phien == null || phien.getId() == null || tin == null) {
            return;
        }
        String tenKhach = "Khách vãng lai";
        try {
            if (phien.getIdKhachHang() != null && phien.getIdKhachHang().getHoTen() != null) {
                tenKhach = phien.getIdKhachHang().getHoTen();
            }
        } catch (Exception ignored) {
            // lazy
        }
        String noiDung = tin.getNoiDung() != null ? tin.getNoiDung().trim() : "";
        if (noiDung.length() > 80) {
            noiDung = noiDung.substring(0, 80) + "…";
        }
        HoTroRealtimeEvent payload = HoTroRealtimeEvent.builder()
                .type(HoTroRealtimeEvent.TYPE_TIN_NHAN_MOI)
                .idPhien(phien.getId())
                .tenKhachHang(tenKhach)
                .noiDungTomTat(noiDung)
                .tinNhan(TinNhanHoTroResponse.from(tin))
                .build();
        eventPublisher.publishEvent(new HoTroRealtimeAppEvent(this, payload));
    }
}
