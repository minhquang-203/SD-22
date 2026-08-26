package org.example.templatejava6.voucher.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Bộ lọc gán voucher theo nhóm khách hàng. Tất cả trường đều optional và
 * được kết hợp bằng AND; để trống toàn bộ nghĩa là chọn mọi khách đang hoạt động.
 */
@Getter
@Setter
public class GanVoucherTheoNhomRequest {

    /** Điểm tích lũy tối thiểu. */
    private Integer diemToiThieu;

    /** Khách mới: đăng ký trong vòng N ngày gần đây. */
    private Integer soNgayKhachMoi;

    /** Sinh nhật theo tháng (1-12). */
    private Integer thangSinhNhat;

    /** Giới tính (Nam / Nu / Khac). */
    private String gioiTinh;

    /** Loại da. */
    private Integer idLoaiDa;
}
