package org.example.templatejava6.staff.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhanVienTrangThaiRequest {

    @NotNull(message = "Trạng thái là bắt buộc")
    private Boolean trangThai;
}
