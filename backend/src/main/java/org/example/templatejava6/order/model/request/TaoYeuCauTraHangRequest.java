package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaoYeuCauTraHangRequest {

    @NotBlank(message = "Vui lòng nhập lý do trả hàng")
    private String lyDo;

    private String moTa;

    /** Dia chi lay hang tra (GHN). Neu de trong se lay theo dia chi giao cua don. */
    private Integer ghnDistrictId;
    private String ghnWardCode;
    private String diaChiTra;

    /** Thong tin nhan tien hoan (bat buoc voi don COD, khong co giao dich online de hoan). */
    private String tenNganHang;
    private String soTaiKhoan;
    private String chuTaiKhoan;
}
