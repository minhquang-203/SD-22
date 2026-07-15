package org.example.templatejava6.notification.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThongBaoTongQuanResponse {

    private long soChuaDoc;
    private List<ThongBaoResponse> danhSach;

    public ThongBaoTongQuanResponse(long soChuaDoc, List<ThongBaoResponse> danhSach) {
        this.soChuaDoc = soChuaDoc;
        this.danhSach = danhSach;
    }
}
