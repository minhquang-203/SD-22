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
public class DanhMucRequest {

    private Integer id;

    @NotBlank(message = "Mã danh mục không được để trống")
    private String ma;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String ten;

    private String moTa;
}
