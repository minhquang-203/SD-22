package org.example.templatejava6.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Định kỳ đóng yêu cầu trả hàng đã duyệt nhưng khách không gửi hàng hoàn quá hạn cửa hàng.
 * Đơn trở về {@code HOAN_THANH}; chưa nhập kho / chưa hoàn tiền.
 */
@Component
public class ReturnOverdueCloseScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReturnOverdueCloseScheduler.class);

    private final ReturnRequestService returnRequestService;

    public ReturnOverdueCloseScheduler(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @Scheduled(fixedDelayString = "${return.overdue-close-scan-ms:300000}")
    public void dongYeuCauTraHangQuaHan() {
        try {
            int closed = returnRequestService.dongCacYeuCauQuaHanGuiHang();
            if (closed > 0) {
                log.info("Đã đóng {} yêu cầu trả hàng quá hạn gửi hàng hoàn.", closed);
            }
        } catch (Exception ex) {
            log.error("Lỗi khi đóng yêu cầu trả hàng quá hạn: {}", ex.getMessage(), ex);
        }
    }
}
