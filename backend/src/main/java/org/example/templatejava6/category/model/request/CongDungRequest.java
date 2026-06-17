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
public class CongDungRequest {

    private Integer id;

    private String ma;

    @NotBlank(message = "Tên công dụng không được để trống")
    private String ten;

    private String moTa;

    private Boolean trangThai;
}
