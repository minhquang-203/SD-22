package org.example.templatejava6.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Đặt hàng online cho khách chưa đăng nhập. Giỏ hàng gửi trực tiếp danh sách biến thể
 * (không dùng giỏ hàng phía server). Đơn tạo ra có id_khach_hang = NULL.
 */
@Getter
@Setter
public class GuestCheckoutRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm")
    private List<@Valid GuestCheckoutItemRequest> items;

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String maPhuongThucThanhToan;

    @NotBlank(message = "Vui lòng nhập họ tên người nhận")
    private String tenNguoiNhan;

    @NotBlank(message = "Vui lòng nhập số điện thoại người nhận")
    private String sdtNguoiNhan;

    @NotBlank(message = "Vui lòng nhập email nhận thông tin đơn hàng")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String diaChiGiao;

    private String ghiChu;

    /** Địa chỉ cụ thể (số nhà, đường). */
    @NotBlank(message = "Vui lòng nhập địa chỉ cụ thể")
    private String toAddressV2;

    /** Tên tỉnh/thành — địa chỉ 2 cấp (GHN to_province_name). */
    private String toProvinceName;

    /** Province _id GHN v3 (to_province_id_v2). */
    private Integer toProvinceId;

    /** Tên phường/xã — địa chỉ 2 cấp (GHN to_ward_name). */
    private String toWardName;

    /** Legacy: mã quận/huyện GHN cũ. */
    private Integer toDistrictId;

    /** Mã phường/xã lưu trên hóa đơn (ward id v3 hoặc ward code cũ). */
    @NotBlank(message = "Vui lòng chọn phường/xã giao hàng")
    private String toWardCode;

    /** Khóa idempotency do client sinh cho mỗi lần đặt hàng (chống double-submit). */
    private String idempotencyKey;
}
