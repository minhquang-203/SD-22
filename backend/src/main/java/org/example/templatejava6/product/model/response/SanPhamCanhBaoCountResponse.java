package org.example.templatejava6.product.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamCanhBaoCountResponse {

    /** Số SP có tổng tồn &lt; 50. */
    private long sapHetHang;
    /** Số SP có ít nhất 1 lô cận hạn (&lt; 6 tháng). */
    private long canHan;
}
