package org.example.templatejava6.realtime.service;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.realtime.event.OrderRealtimeAppEvent;
import org.example.templatejava6.realtime.model.OrderRealtimeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class OrderRealtimeService {

    private final ApplicationEventPublisher eventPublisher;

    public OrderRealtimeService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishCreated(HoaDon hoaDon) {
        publish(hoaDon, OrderRealtimeEvent.TYPE_CREATED, null, "Đơn hàng mới: " + safeMa(hoaDon));
    }

    public void publishStatusChanged(HoaDon hoaDon, TrangThaiDonHang trangThaiCu) {
        String cu = trangThaiCu != null ? trangThaiCu.name() : null;
        String moi = hoaDon.getTrangThai() != null ? mapTrangThaiLabel(hoaDon.getTrangThai()) : "";
        publish(hoaDon, OrderRealtimeEvent.TYPE_STATUS_CHANGED, cu,
                "Đơn " + safeMa(hoaDon) + " cập nhật: " + moi);
    }

    private void publish(HoaDon hoaDon, String type, String trangThaiCu, String message) {
        if (hoaDon == null || hoaDon.getId() == null) {
            return;
        }
        TrangThaiDonHang trangThai = hoaDon.getTrangThai();
        Integer idKhach = null;
        try {
            if (hoaDon.getIdKhachHang() != null) {
                idKhach = hoaDon.getIdKhachHang().getId();
            }
        } catch (Exception ignored) {
            // lazy load lỗi sau commit — không chặn push admin
        }
        OrderRealtimeEvent payload = OrderRealtimeEvent.builder()
                .type(type)
                .idHoaDon(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .idKhachHang(idKhach)
                .trangThai(trangThai != null ? trangThai.name() : null)
                .trangThaiLabel(trangThai != null ? mapTrangThaiLabel(trangThai) : null)
                .trangThaiCu(trangThaiCu)
                .loaiDon(hoaDon.getLoaiDon())
                .message(message)
                .build();
        eventPublisher.publishEvent(new OrderRealtimeAppEvent(this, payload));
    }

    private static String mapTrangThaiLabel(TrangThaiDonHang trangThai) {
        if (trangThai == TrangThaiDonHang.HOAN_THANH) {
            return "Đã giao";
        }
        return trangThai.getLabel();
    }

    private static String safeMa(HoaDon hoaDon) {
        return hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon() : String.valueOf(hoaDon.getId());
    }
}
