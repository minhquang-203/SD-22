package org.example.templatejava6.realtime.listener;

import org.example.templatejava6.realtime.event.AdminNotificationAppEvent;
import org.example.templatejava6.realtime.event.OrderRealtimeAppEvent;
import org.example.templatejava6.realtime.model.AdminNotificationEvent;
import org.example.templatejava6.realtime.model.OrderRealtimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RealtimeEventListener {

    public static final String TOPIC_ADMIN_ORDERS = "/topic/admin/orders";
    public static final String TOPIC_ADMIN_NOTIFICATIONS = "/topic/admin/notifications";

    private static final Logger log = LoggerFactory.getLogger(RealtimeEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderEvent(OrderRealtimeAppEvent event) {
        OrderRealtimeEvent payload = event.getPayload();
        if (payload == null) {
            return;
        }
        try {
            messagingTemplate.convertAndSend(TOPIC_ADMIN_ORDERS, payload);
            if (payload.getIdKhachHang() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/customers/" + payload.getIdKhachHang() + "/orders",
                        payload);
            }
        } catch (Exception ex) {
            log.warn("Không gửi được realtime đơn {}: {}", payload.getIdHoaDon(), ex.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAdminNotification(AdminNotificationAppEvent event) {
        AdminNotificationEvent payload = event.getPayload();
        if (payload == null) {
            return;
        }
        try {
            messagingTemplate.convertAndSend(TOPIC_ADMIN_NOTIFICATIONS, payload);
        } catch (Exception ex) {
            log.warn("Không gửi được realtime thông báo admin: {}", ex.getMessage());
        }
    }
}
