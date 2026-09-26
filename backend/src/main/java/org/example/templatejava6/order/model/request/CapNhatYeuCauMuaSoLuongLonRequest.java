package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapNhatYeuCauMuaSoLuongLonRequest {

    @NotBlank(message = "Trạng thái bắt buộc")
    @Size(max = 20)
    private String trangThai;

    @Size(max = 1000)
    private String ghiChuNoiBo;
}
