package org.example.templatejava6.notification.enums;

/** Loại thông báo hiển thị trên chuông admin và chuông khách hàng. */
public enum LoaiThongBao {
    // Thông báo phía admin/hệ thống (id_khach_hang = NULL)
    DON_HANG_MOI,
    THANH_TOAN_THANH_CONG,
    YEU_CAU_TRA_HANG,
    YEU_CAU_HOAN_TIEN,
    HOAN_TIEN_HOAN_TAT,

    // Thông báo gửi riêng cho khách hàng (id_khach_hang != NULL)
    DON_HANG_CAP_NHAT,
    TRA_HANG_DUOC_DUYET,
    TRA_HANG_BI_TU_CHOI,
    HOAN_TIEN_THANH_CONG,
    HOAN_TIEN_BI_TU_CHOI
}
