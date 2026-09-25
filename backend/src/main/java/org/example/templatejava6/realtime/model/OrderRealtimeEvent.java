package org.example.templatejava6.realtime.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderRealtimeEvent {

    public static final String TYPE_CREATED = "ORDER_CREATED";
    public static final String TYPE_STATUS_CHANGED = "ORDER_STATUS_CHANGED";
    /** Cập nhật yêu cầu/vận đơn trả hàng (không đổi trang_thai hóa đơn). */
    public static final String TYPE_RETURN_UPDATED = "RETURN_STATUS_CHANGED";

    private final String type;
    private final Integer idHoaDon;
    private final String maHoaDon;
    private final Integer idKhachHang;
    private final String trangThai;
    private final String trangThaiLabel;
    private final String trangThaiCu;
    private final String loaiDon;
    private final String message;

    private final Integer idYeuCauTraHang;
    private final String trangThaiTraHang;
    private final String trangThaiTraHangLabel;
    private final String maVanDonTra;
    private final String ghnTrangThaiTra;
    private final String ghnTrangThaiTraLabel;
}
