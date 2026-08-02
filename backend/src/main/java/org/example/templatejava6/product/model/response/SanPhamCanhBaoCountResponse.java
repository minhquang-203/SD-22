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

    /** Số SP có tổng tồn ≤ 10. */
    private long sapHetHang;
    /** Số SP có ít nhất 1 lô cận hạn (≤ 30 ngày). */
    private long canHan;
}
