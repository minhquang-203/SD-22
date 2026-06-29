package org.example.templatejava6.staff.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhanVienDatLaiMatKhauRequest {

    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String matKhauMoi;

    private Boolean sinhTuDong;
}
