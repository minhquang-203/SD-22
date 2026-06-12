package org.example.templatejava6.customer.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;

import java.util.List;

@Getter
@Setter
public class KhachHangDetailResponse extends KhachHangResponse {

    private List<DiaChiResponse> diaChis;

    public KhachHangDetailResponse(KhachHang kh) {
        super(kh);
    }
}
