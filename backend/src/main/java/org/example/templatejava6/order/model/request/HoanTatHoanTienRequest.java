package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HoanTatHoanTienRequest {

    private Integer idNhanVien;

    /**
     * Ma giao dich hoan tien — bat buoc cho luong thu cong (CHUYEN_KHOAN).
     * Bo qua khi hoan tu dong qua cong thanh toan (VNPAY).
     */
    private String maGiaoDichHoan;

    /** Cho phep admin dieu chinh so tien hoan truoc khi xac nhan. */
    private BigDecimal soTien;

    private String ghiChu;
}
