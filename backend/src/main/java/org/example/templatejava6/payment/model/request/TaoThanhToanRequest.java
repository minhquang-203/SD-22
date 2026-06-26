package org.example.templatejava6.payment.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaoThanhToanRequest {

    @NotNull(message = "Vui lòng chọn hóa đơn cần thanh toán")
    private Integer idHoaDon;
}
