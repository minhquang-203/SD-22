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
    /** Số SP có ít nhất 1 lô cận hạn (≤ 30 ngày). */
    private long canHan;
    /** Số lô đã hết hạn còn hàng. */
    private long hetHan;
}
