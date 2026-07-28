package org.example.templatejava6.order.service;

import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.repository.YeuCauTraHangRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dinh ky doi trang thai van don hoan ben GHN cho cac yeu cau tra hang dang hoan hang.
 * Khi kien hang ve tay shop, {@link ReturnRequestService} tu ghi nhan da nhan hang
 * va tao yeu cau hoan tien cho admin quyet dinh.
 */
@Component
public class ReturnShipmentSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReturnShipmentSyncScheduler.class);

    private final YeuCauTraHangRepository yeuCauTraHangRepository;
    private final ReturnRequestService returnRequestService;

    public ReturnShipmentSyncScheduler(YeuCauTraHangRepository yeuCauTraHangRepository,
                                       ReturnRequestService returnRequestService) {
        this.yeuCauTraHangRepository = yeuCauTraHangRepository;
        this.returnRequestService = returnRequestService;
    }

    @Scheduled(fixedDelayString = "${return.shipment.sync-ms:300000}")
    public void dongBoVanDonHoan() {
        List<YeuCauTraHang> dangHoan = yeuCauTraHangRepository
                .findByTrangThaiOrderByNgayTaoDesc(TrangThaiTraHang.DANG_HOAN_HANG);
        for (YeuCauTraHang yc : dangHoan) {
            if (yc.getMaVanDonTra() == null || yc.getMaVanDonTra().isBlank()) {
                continue;
            }
            try {
                returnRequestService.dongBoVanDonTra(yc.getId(), null);
            } catch (Exception ex) {
                log.warn("Không đồng bộ được vận đơn hoàn {} (yêu cầu #{}): {}",
                        yc.getMaVanDonTra(), yc.getId(), ex.getMessage());
            }
        }
    }
}
