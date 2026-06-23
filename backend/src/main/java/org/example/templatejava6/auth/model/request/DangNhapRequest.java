package org.example.templatejava6.auth.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DangNhapRequest {

    @NotBlank(message = "Vui lòng nhập email hoặc số điện thoại")
    @Size(max = 100, message = "Tài khoản tối đa 100 ký tự")
    private String taiKhoan;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 100, message = "Mật khẩu tối đa 100 ký tự")
    private String matKhau;
}
