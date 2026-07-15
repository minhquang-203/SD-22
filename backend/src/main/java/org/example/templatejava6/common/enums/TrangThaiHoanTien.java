package org.example.templatejava6.common.enums;

import lombok.Getter;

/** Trang thai cua mot ban ghi hoan tien. */
@Getter
public enum TrangThaiHoanTien {
    CHO_XU_LY("Chờ xử lý"),
    DA_HOAN("Đã hoàn tiền"),
    TU_CHOI("Từ chối");

    private final String label;

    TrangThaiHoanTien(String label) {
        this.label = label;
    }
}
