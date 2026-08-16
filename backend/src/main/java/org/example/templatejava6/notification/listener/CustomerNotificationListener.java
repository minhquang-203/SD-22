package org.example.templatejava6.notification.listener;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.realtime.event.OrderRealtimeAppEvent;
import org.example.templatejava6.realtime.model.OrderRealtimeEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Tạo bản ghi thông báo cho khách hàng mỗi khi đơn của họ đổi trạng thái.
 * Lắng nghe cùng sự kiện realtime của đơn hàng nên bao phủ mọi luồng đổi trạng thái
 * (admin cập nhật, khách hủy, đồng bộ GHN...).
 *
 * <p>Bỏ qua trạng thái {@code TRA_HANG} vì luồng duyệt trả hàng / hoàn tiền đã có
 * thông báo riêng, rõ ngữ cảnh hơn cho khách.</p>
 */
@Component
public class CustomerNotificationListener {

    private static final String LINK_DON_HANG = "/tra-cuu-don";

    private final ThongBaoService thongBaoService;

    public CustomerNotificationListener(ThongBaoService thongBaoService) {
        this.thongBaoService = thongBaoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderStatusChanged(OrderRealtimeAppEvent event) {
        OrderRealtimeEvent payload = event.getPayload();
        if (payload == null
                || !OrderRealtimeEvent.TYPE_STATUS_CHANGED.equals(payload.getType())
                || payload.getIdKhachHang() == null) {
            return;
        }
        if (TrangThaiDonHang.TRA_HANG.name().equals(payload.getTrangThai())) {
            return;
        }

        String ma = payload.getMaHoaDon() != null ? payload.getMaHoaDon() : ("#" + payload.getIdHoaDon());
        String noiDung = TrangThaiDonHang.DA_HUY.name().equals(payload.getTrangThai())
                ? "Đơn " + ma + " đã bị hủy."
                : "Đơn " + ma + " đã chuyển sang: " + nhan(payload) + ".";

        thongBaoService.taoThongBaoKhach(
                payload.getIdKhachHang(),
                LoaiThongBao.DON_HANG_CAP_NHAT,
                "Cập nhật đơn hàng",
                noiDung,
                LINK_DON_HANG,
                payload.getIdHoaDon(),
                payload.getMaHoaDon());
    }

    private static String nhan(OrderRealtimeEvent payload) {
        if (payload.getTrangThaiLabel() != null && !payload.getTrangThaiLabel().isBlank()) {
            return payload.getTrangThaiLabel();
        }
        return payload.getTrangThai() != null ? payload.getTrangThai() : "";
    }
}
