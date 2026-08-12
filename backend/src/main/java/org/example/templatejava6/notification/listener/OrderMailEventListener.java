package org.example.templatejava6.notification.listener;

import org.example.templatejava6.notification.event.DatHangThanhCongMailEvent;
import org.example.templatejava6.notification.service.OrderMailService;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Gửi mail hóa đơn SAU khi transaction thanh toán/checkout commit, trong một transaction mới
 * (để nạp lại hóa đơn + quan hệ lazy). Nhờ vậy không giữ khóa bản ghi thanh toán trong lúc gọi SMTP.
 */
@Component
public class OrderMailEventListener {

    private final OrderMailService orderMailService;
    private final HoaDonRepository hoaDonRepository;

    public OrderMailEventListener(OrderMailService orderMailService, HoaDonRepository hoaDonRepository) {
        this.orderMailService = orderMailService;
        this.hoaDonRepository = hoaDonRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onDatHangThanhCong(DatHangThanhCongMailEvent event) {
        if (event.getIdHoaDon() == null) {
            return;
        }
        hoaDonRepository.findById(event.getIdHoaDon())
                .ifPresent(orderMailService::guiHoaDonDatHangThanhCong);
    }
}
