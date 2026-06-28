package org.example.templatejava6.order.service;

import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OnlineOrderTimeoutScheduler {

    private final HoaDonRepository hoaDonRepository;
    private final OnlineOrderLifecycleService onlineOrderLifecycleService;
    private final long paymentTimeoutMinutes;

    public OnlineOrderTimeoutScheduler(
            HoaDonRepository hoaDonRepository,
            OnlineOrderLifecycleService onlineOrderLifecycleService,
            @Value("${online.order.payment-timeout-minutes:15}") long paymentTimeoutMinutes) {
        this.hoaDonRepository = hoaDonRepository;
        this.onlineOrderLifecycleService = onlineOrderLifecycleService;
        this.paymentTimeoutMinutes = paymentTimeoutMinutes;
    }

    @Scheduled(fixedDelayString = "${online.order.timeout-scan-ms:60000}")
    public void huyDonVnpayQuaHan() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        List<HoaDon> expiredOrders = hoaDonRepository.findExpiredUnpaidVnpayOrders(cutoff);
        for (HoaDon hoaDon : expiredOrders) {
            if (!onlineOrderLifecycleService.daThanhToanThanhCong(hoaDon)) {
                onlineOrderLifecycleService.huyDonOnline(
                        hoaDon,
                        "Đơn VNPAY quá hạn " + paymentTimeoutMinutes + " phút chưa thanh toán.");
            }
        }
    }
}
