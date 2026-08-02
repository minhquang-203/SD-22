package org.example.templatejava6.nhaphang.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhaCungCapRequest {

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String ten;

    private String soDienThoai;
    private String email;
    private String diaChi;
    private String ghiChu;
}
