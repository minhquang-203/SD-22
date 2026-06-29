package org.example.templatejava6.staff.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NhanVienUpdateRequest {

    @NotBlank(message = "Họ tên là bắt buộc")
    @Size(max = 100)
    private String hoTen;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Size(max = 15)
    private String soDienThoai;

    @NotNull(message = "Vai trò là bắt buộc")
    @Pattern(regexp = "NHAN_VIEN|QUAN_LY", message = "Vai trò không hợp lệ")
    private String maVaiTro;

    @Size(max = 10)
    private String gioiTinh;

    private LocalDate ngayVaoLam;
}
