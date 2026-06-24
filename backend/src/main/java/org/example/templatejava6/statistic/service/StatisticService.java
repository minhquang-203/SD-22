package org.example.templatejava6.statistic.service;

import org.example.templatejava6.statistic.model.response.TongQuanResponse;
import org.example.templatejava6.statistic.repository.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class StatisticService {

    @Autowired
    private StatisticRepository statisticRepository;

    public TongQuanResponse getTongQuan(LocalDateTime fromDate, LocalDateTime toDate) {
        Object[] result = statisticRepository.getTongQuanThongKe(fromDate, toDate);
        Object[] row = (Object[]) result[0];

        // Ép kiểu theo đúng thứ tự Index trong câu SQL mới
        BigDecimal totalRev = row[0] != null ? new BigDecimal(row[0].toString()) : BigDecimal.ZERO;
        BigDecimal actualRev = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
        BigDecimal expectedRev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
        Integer totalOrders = row[3] != null ? Integer.parseInt(row[3].toString()) : 0;
        Integer webOrders = row[4] != null ? Integer.parseInt(row[4].toString()) : 0;
        Integer posOrders = row[5] != null ? Integer.parseInt(row[5].toString()) : 0;

        return TongQuanResponse.builder()
                .totalRevenue(totalRev)
                .actualRevenue(actualRev)
                .expectedRevenue(expectedRev)
                .totalOrders(totalOrders)
                .webOrders(webOrders)
                .posOrders(posOrders)
                .build();
    }
}