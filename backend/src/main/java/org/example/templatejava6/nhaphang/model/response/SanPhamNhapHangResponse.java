package org.example.templatejava6.nhaphang.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.SanPham;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SanPhamNhapHangResponse {

    private Integer idSanPham;
    private String maSanPham;
    private String tenSanPham;
    private String anhUrl;
    private Integer soBienThe;
    private List<BienTheNhapHangResponse> bienThes = new ArrayList<>();

    public SanPhamNhapHangResponse(SanPham sp) {
        this.idSanPham = sp.getId();
        this.maSanPham = sp.getMaSanPham();
        this.tenSanPham = sp.getTen();
    }
}
