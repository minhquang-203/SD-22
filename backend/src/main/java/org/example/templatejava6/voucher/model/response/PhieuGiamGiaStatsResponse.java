package org.example.templatejava6.voucher.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhieuGiamGiaStatsResponse {

    private long active;
    private long usageCount;
    private BigDecimal totalSavings;
    private long expiringSoon;
}
