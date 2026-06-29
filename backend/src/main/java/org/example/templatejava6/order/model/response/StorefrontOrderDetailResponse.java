package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class StorefrontOrderDetailResponse {

    private Integer id;
    private String maHoaDon;
    private LocalDateTime ngayTao;
    private String trangThai;
    private String trangThaiLabel;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal thanhTien;
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiGiao;
    private List<StorefrontOrderLineResponse> chiTiets;
    private String capNhatGanNhatTrangThai;
    private String capNhatGanNhatLabel;
    private LocalDateTime capNhatGanNhatLuc;
}
