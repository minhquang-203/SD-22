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

    /** Mã vận đơn GHN (nếu đã tạo) — để FE vẫn hiện VC khi chi tiết lỗi/timeout. */
    private String maVanDon;
    private String donViVanChuyen;

    /** Preview sản phẩm đầu tiên — dùng cho thẻ danh sách phía khách. */
    private String anhUrl;
    private String tenSanPham;
    private Integer soLuong;
    private Integer soDongHang;

    /** Phương thức thanh toán — cần để mở modal trả hàng từ danh sách. */
    private String maPhuongThucThanhToan;

    /** Yêu cầu trả hàng mới nhất (nếu có) — để lọc tab / điều hướng không cần hydrate chi tiết. */
    private Integer idYeuCauTraHang;
    private String trangThaiTraHang;
    private String trangThaiTraHangLabel;
    private String maVanDonTra;
}
