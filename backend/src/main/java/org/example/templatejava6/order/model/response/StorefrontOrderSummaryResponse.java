package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class StorefrontOrderSummaryResponse {

    private Integer id;
    private String maHoaDon;
    private LocalDateTime ngayTao;
    private String trangThai;
    private String trangThaiLabel;
    private BigDecimal thanhTien;

    /** Preview sản phẩm đầu tiên — dùng cho thẻ danh sách phía khách. */
    private String anhUrl;
    private String tenSanPham;
    private Integer soLuong;
    private Integer soDongHang;
}
