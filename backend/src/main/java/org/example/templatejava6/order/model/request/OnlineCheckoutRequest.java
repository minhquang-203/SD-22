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

    /** ID phường/xã đơn vị hành chính mới (GHN v3). */
    private Integer toWardIdV2;

    /** Địa chỉ text cụ thể để tính phí (GHN to_address_v2). */
    @NotBlank(message = "Vui lòng nhập địa chỉ cụ thể")
    private String toAddressV2;

    /** Tên tỉnh/thành (địa chỉ mới). */
    private String toProvinceName;

    /** Tên phường/xã (địa chỉ mới). */
    private String toWardName;

    /** Legacy: mã quận/huyện GHN cũ — không bắt buộc với địa chỉ 2 cấp. */
    private Integer toDistrictId;

    /**
     * Mã phường/xã: với địa chỉ mới là String.valueOf(toWardIdV2).
     * Vẫn bắt buộc để lưu lên hóa đơn / tạo vận đơn.
     */
    @NotBlank(message = "Vui lòng chọn phường/xã giao hàng")
    private String toWardCode;
}
