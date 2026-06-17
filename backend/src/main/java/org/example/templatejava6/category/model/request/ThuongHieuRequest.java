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
public class ThuongHieuRequest {

    private Integer id;

    private String ma;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String ten;

    private String xuatXu;

    private Boolean trangThai;
}
