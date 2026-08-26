package org.example.templatejava6.voucher.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Gán voucher cá nhân cho một hoặc nhiều khách hàng cụ thể. */
@Getter
@Setter
public class GanVoucherRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một khách hàng")
    private List<Integer> idKhachHangs;
}
