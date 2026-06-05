package org.example.templatejava6.product.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnhSanPhamRequest {

    @NotBlank(message = "URL ảnh không được để trống")
    private String url;

    private Boolean laAnhChinh;
    private Integer thuTu;
}
