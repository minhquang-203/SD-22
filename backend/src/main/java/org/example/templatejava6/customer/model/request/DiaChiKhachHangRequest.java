package org.example.templatejava6.customer.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaChiKhachHangRequest {

    @NotBlank(message = "Họ tên người nhận không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    private String soDienThoai;

    @NotNull(message = "Vui lòng chọn tỉnh / thành phố")
    private Integer provinceId;

    @NotNull(message = "Vui lòng chọn quận / huyện")
    private Integer districtId;

    @NotBlank(message = "Vui lòng chọn phường / xã")
    @Size(max = 20, message = "Mã phường / xã không hợp lệ")
    private String wardCode;

    @NotBlank(message = "Tỉnh / thành phố không được để trống")
    @Size(max = 50, message = "Tỉnh / thành phố tối đa 50 ký tự")
    private String tinhThanh;

    @NotBlank(message = "Quận / huyện không được để trống")
    @Size(max = 50, message = "Quận / huyện tối đa 50 ký tự")
    private String quanHuyen;

    @NotBlank(message = "Phường / xã không được để trống")
    @Size(max = 50, message = "Phường / xã tối đa 50 ký tự")
    private String phuongXa;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(max = 255, message = "Địa chỉ cụ thể tối đa 255 ký tự")
    private String diaChiChiTiet;

    private Boolean macDinh;
}
