package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnlineCheckoutRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm trong giỏ hàng")
    private List<@NotNull Integer> idsChiTietGioHang;

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String maPhuongThucThanhToan;

    private String maPhieuGiamGia;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String diaChiGiao;

    private String ghiChu;

    /** Ten nguoi nhan thuc te (de tao van don GHN). Neu trong se lay theo tai khoan. */
    private String tenNguoiNhan;

    /** So dien thoai nguoi nhan thuc te. Neu trong se lay theo tai khoan. */
    private String sdtNguoiNhan;

    /** Địa chỉ cụ thể (số nhà, đường). */
    @NotBlank(message = "Vui lòng nhập địa chỉ cụ thể")
    private String toAddressV2;

    /** Tên tỉnh/thành — địa chỉ 2 cấp (GHN to_province_name). */
    private String toProvinceName;

    /** Tên phường/xã — địa chỉ 2 cấp (GHN to_ward_name). */
    private String toWardName;

    /** Legacy: mã quận/huyện GHN cũ — không bắt buộc với địa chỉ 2 cấp. */
    private Integer toDistrictId;

    /**
     * Mã phường/xã lưu trên hóa đơn (ward id v3 hoặc ward code cũ).
     * Tạo vận đơn 2 cấp dùng tên, không gửi mã này lên GHN.
     */
    @NotBlank(message = "Vui lòng chọn phường/xã giao hàng")
    private String toWardCode;

    /**
     * Khóa idempotency do client sinh cho mỗi lần đặt hàng. Gửi lại cùng key (double-click,
     * retry mạng) sẽ trả về đúng đơn đã tạo thay vì tạo đơn mới. Không bắt buộc để giữ tương thích.
     */
    private String idempotencyKey;
}
