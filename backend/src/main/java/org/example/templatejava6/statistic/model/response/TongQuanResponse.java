package org.example.templatejava6.statistic.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TongQuanResponse {
    private BigDecimal totalRevenue;     // Tổng doanh thu
    private BigDecimal actualRevenue;    // Doanh thu thực tế
    private BigDecimal expectedRevenue;  // Doanh thu dự kiến
    private Integer totalOrders;         // Tổng số đơn
    private Integer webOrders;           // Đơn từ Web
    private Integer posOrders;           // Đơn từ POS
}