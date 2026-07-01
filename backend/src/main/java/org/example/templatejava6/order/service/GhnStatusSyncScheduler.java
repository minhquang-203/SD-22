package org.example.templatejava6.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dinh ky quet cac don dang giao co ma van don GHN va dong bo trang thai don hang
 * theo trang thai van don ben GHN.
 *
 * <p>Chu ky cau hinh qua {@code ghn.sync.fixed-delay-ms} (mac dinh 15 phut). Co the tat
 * bang {@code ghn.sync.enabled=false}.</p>
 */
@Component
public class GhnStatusSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(GhnStatusSyncScheduler.class);

    private final GhnOrderSyncService ghnOrderSyncService;
    private final boolean enabled;

    public GhnStatusSyncScheduler(GhnOrderSyncService ghnOrderSyncService,
                                  @Value("${ghn.sync.enabled:true}") boolean enabled) {
        this.ghnOrderSyncService = ghnOrderSyncService;
        this.enabled = enabled;
    }

    @Scheduled(
            fixedDelayString = "${ghn.sync.fixed-delay-ms:900000}",
            initialDelayString = "${ghn.sync.initial-delay-ms:60000}")
    public void dongBoTrangThaiGhn() {
        if (!enabled) {
            return;
        }
        List<Integer> ids = ghnOrderSyncService.layIdDonCanDongBo();
        if (ids.isEmpty()) {
            return;
        }
        int capNhat = 0;
        for (Integer id : ids) {
            try {
                if (ghnOrderSyncService.dongBoTheoId(id).daCapNhat()) {
                    capNhat++;
                }
            } catch (Exception ex) {
                log.warn("Dong bo GHN that bai cho don id={}: {}", id, ex.getMessage());
            }
        }
        if (capNhat > 0) {
            log.info("Dong bo GHN: quet {} don, cap nhat {} don.", ids.size(), capNhat);
        }
    }
}
