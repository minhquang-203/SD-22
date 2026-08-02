package org.example.templatejava6.nhaphang.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PhieuNhapRequest {

    private Integer idNhaCungCap;
    /** Ngày nhập kho (map vào ngay_tao của phiếu). */
    private LocalDate ngayNhap;
    private String soHoaDonDauVao;
    private BigDecimal giamGia;
    private String ghiChu;
    private List<DongPhieuNhapRequest> chiTiets = new ArrayList<>();

    @Getter
    @Setter
    public static class DongPhieuNhapRequest {
        private Integer idChiTietSanPham;
        private Integer soLuong;
        private BigDecimal donGia;
        private LocalDate hanSuDung;
        private String soLo;
    }
}
