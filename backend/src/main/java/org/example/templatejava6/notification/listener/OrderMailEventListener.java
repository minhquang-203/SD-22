package org.example.templatejava6.notification.listener;

import org.example.templatejava6.notification.event.DatHangThanhCongMailEvent;
import org.example.templatejava6.notification.service.OrderMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executor;

/**
 * Sau khi checkout/thanh toán commit: xếp hàng gửi mail hóa đơn trên thread riêng.
 * SMTP (Gmail) thường mất vài giây tới ~30s — nếu chạy đồng bộ trên request thread
 * thì khách đã đặt COD xong vẫn phải chờ mới thấy trang thành công.
 */
@Component
public class OrderMailEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderMailEventListener.class);

    private final OrderMailService orderMailService;
    private final Executor mailExecutor;

    public OrderMailEventListener(OrderMailService orderMailService,
                                  @Qualifier("mailExecutor") Executor mailExecutor) {
        this.orderMailService = orderMailService;
        this.mailExecutor = mailExecutor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDatHangThanhCong(DatHangThanhCongMailEvent event) {
        Integer idHoaDon = event.getIdHoaDon();
        if (idHoaDon == null) {
            return;
        }
        mailExecutor.execute(() -> {
            try {
                orderMailService.guiHoaDonDatHangThanhCong(idHoaDon);
            } catch (Exception ex) {
                log.error("[MAIL] Lỗi luồng nền gửi hóa đơn đơn {}: {}", idHoaDon, ex.getMessage(), ex);
            }
        });
    }
}
