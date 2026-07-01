package org.example.templatejava6.voucher.repository;

import java.math.BigDecimal;

public interface SalePriceAgg {

    Integer getSanPhamId();

    BigDecimal getGiaGocMin();

    BigDecimal getGiaGocMax();

    BigDecimal getGiaSauGiamMin();

    BigDecimal getGiaSauGiamMax();

    BigDecimal getPhanTramGiam();
}
