package org.example.templatejava6.category.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThanhPhanRequest {

    private Integer id;

    @NotBlank(message = "Mã thành phần không được để trống")
    private String ma;

    @NotBlank(message = "Tên thành phần không được để trống")
    private String ten;

    private String loai;
    private String moTa;
}
