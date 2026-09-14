package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TraCuuDonRequest {

    @NotBlank(message = "Vui lòng nhập mã đơn hàng")
    private String ma;

    @NotBlank(message = "Vui lòng nhập email đặt hàng")
    @Email(message = "Email không hợp lệ")
    private String email;
}
