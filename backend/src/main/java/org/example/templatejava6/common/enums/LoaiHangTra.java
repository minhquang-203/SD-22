package org.example.templatejava6.common.enums;

public enum LoaiHangTra {
    TOT("Hàng còn tốt"),
    LOI("Hàng lỗi/hỏng");

    private final String label;

    LoaiHangTra(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
