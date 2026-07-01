package org.example.templatejava6.voucher.repository;

import java.math.BigDecimal;

public interface VariantSalePriceRow {

    Integer getChiTietSanPhamId();

    BigDecimal getGiaGoc();

    BigDecimal getGiaSauGiam();

    BigDecimal getPhanTramGiam();
}
