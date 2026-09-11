package org.example.templatejava6.common.enums;

import lombok.Getter;

/** Trang thai cua mot yeu cau tra hang / hoan tra hang tu khach hang. */
@Getter
public enum TrangThaiTraHang {
    CHO_DUYET("Chờ duyệt"),
    DA_DUYET("Đã duyệt"),
    TU_CHOI("Từ chối"),
    DANG_HOAN_HANG("Đang hoàn hàng"),
    DA_NHAN_HANG("Đã nhận hàng"),
    HOAN_TAT("Hoàn tất");

    private final String label;

    TrangThaiTraHang(String label) {
        this.label = label;
    }

    /**
     * Nhãn hiển thị phía khách — tránh nhầm “đã nhận hàng” (khách) / “hoàn tất” (đơn).
     */
    public String getLabelChoKhach() {
        return switch (this) {
            case DA_NHAN_HANG -> "Shop đã nhận";
            case HOAN_TAT -> "Trả hàng";
            default -> label;
        };
    }

    public boolean laTrangThaiKetThuc() {
        return this == TU_CHOI || this == HOAN_TAT;
    }
}
