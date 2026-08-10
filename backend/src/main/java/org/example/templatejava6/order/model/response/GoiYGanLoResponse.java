package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GoiYGanLoResponse {

    private Integer idHoaDon;
    private String maHoaDon;
    private String tenKhachHang;
    private LocalDateTime ngayTao;
    private List<DongHangGoiY> dongHang = new ArrayList<>();

    @Getter
    @Setter
    public static class DongHangGoiY {
        private Integer idHoaDonChiTiet;
        private Integer idChiTietSanPham;
        private String tenSanPham;
        private String sku;
        private String bienThe;
        private String anhUrl;
        private Integer soLuong;
        private List<LoDaGanItem> loDaGan = new ArrayList<>();
        private List<LoCoTheChonItem> loCoTheChon = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class LoDaGanItem {
        private Integer idLoHang;
        private String soLo;
        private LocalDate hanSuDung;
        private LocalDate ngayNhap;
        private Integer soLuong;
    }

    @Getter
    @Setter
    public static class LoCoTheChonItem {
        private Integer id;
        private String soLo;
        private LocalDate ngayNhap;
        private LocalDate hanSuDung;
        /** Tồn hiện tại trên kho (đã trừ phần giữ của đơn này). */
        private Integer soLuongCon;
        /**
         * Số lượng tối đa admin có thể chọn cho lô này trên dòng:
         * soLuongCon + số đã gán trên dòng hiện tại.
         */
        private Integer soLuongCoTheChon;
        private Boolean sapHetHan;
    }
}
