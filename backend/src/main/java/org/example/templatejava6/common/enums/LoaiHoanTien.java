package org.example.templatejava6.common.enums;

import lombok.Getter;

/** Nguon goc cua mot ban ghi hoan tien. */
@Getter
public enum LoaiHoanTien {
    HUY_DON("Hủy đơn đã thanh toán"),
    TRA_HANG("Trả hàng sau khi nhận");

    private final String label;

    LoaiHoanTien(String label) {
        this.label = label;
    }
}
