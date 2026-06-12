package org.example.templatejava6.common.enums;

import lombok.Getter;

@Getter
public enum TrangThaiDonHang {
    CHO(-2, "Đơn chờ tại quầy"),
    CHO_XAC_NHAN(0, "Chờ xác nhận"),
    DA_XAC_NHAN(1, "Đã xác nhận"),
    DANG_CHUAN_BI(2, "Đang chuẩn bị hàng"),
    DANG_GIAO(3, "Đang giao"),
    HOAN_THANH(4, "HOAN_THANH"),
    DA_HUY(-1, "Đã hủy");

    private final int thuTu;
    private final String label;

    TrangThaiDonHang(int thuTu, String label) {
        this.thuTu = thuTu;
        this.label = label;
    }

    public boolean laTrangThaiKetThuc() {
        return this == HOAN_THANH || this == DA_HUY;
    }

    public boolean coTheChuyenSang(TrangThaiDonHang trangThaiMoi) {
        if (trangThaiMoi == null || this == trangThaiMoi) {
            return false;
        }
        if (laTrangThaiKetThuc()) {
            return false;
        }
        if (trangThaiMoi == DA_HUY) {
            return true;
        }
        return trangThaiMoi.thuTu > this.thuTu;
    }
}
