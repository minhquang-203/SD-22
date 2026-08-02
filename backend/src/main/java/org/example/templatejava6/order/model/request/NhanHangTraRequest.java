package org.example.templatejava6.order.model.request;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.enums.LoaiHangTra;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NhanHangTraRequest {

    private Integer idNhanVien;
    private String ghiChu;
    private List<ChiTietLoRequest> chiTietLo = new ArrayList<>();

    @Getter
    @Setter
    public static class ChiTietLoRequest {
        private Integer idLoHang;
        private Integer soLuong;
        private LoaiHangTra loaiHang;
    }
}
