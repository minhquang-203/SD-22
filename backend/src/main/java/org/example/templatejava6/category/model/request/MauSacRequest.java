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
public class MauSacRequest {

    private Integer id;

    private String ma;

    @NotBlank(message = "Tên màu sắc không được để trống")
    private String ten;

    private String maHex;

    private Boolean trangThai;
}
