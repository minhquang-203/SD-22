package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaoYeuCauMuaSoLuongLonRequest {

    @NotNull(message = "Thiếu sản phẩm")
    private Integer idChiTietSanPham;

    @NotNull(message = "Số lượng bắt buộc")
    @Min(value = 16, message = "Số lượng tư vấn phải lớn hơn 15")
    private Integer soLuong;

    @Size(max = 100, message = "Tên công ty tối đa 100 ký tự")
    private String tenCongTy;

    @NotBlank(message = "Họ tên bắt buộc")
    @Size(min = 2, max = 60, message = "Họ tên từ 2–60 ký tự")
    private String hoTen;

    @NotBlank(message = "Số điện thoại bắt buộc")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)")
    private String soDienThoai;

    @NotBlank(message = "Email bắt buộc")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;

    private Boolean nhanKhuyenMai;
}
