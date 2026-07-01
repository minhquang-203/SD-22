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
public class VariantSaleInfo {

    private BigDecimal giaGoc;
    private BigDecimal giaSauGiam;
    private BigDecimal phanTramGiam;
}
