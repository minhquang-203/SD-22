package org.example.templatejava6.notification.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.notification.entity.ThongBao;

import java.time.LocalDateTime;

@Getter
@Setter
public class ThongBaoResponse {

    private Integer id;
    private String loai;
    private String tieuDe;
    private String noiDung;
    private String link;
    private Integer idThamChieu;
    private String maThamChieu;
    private Boolean daDoc;
    private LocalDateTime ngayTao;

    public ThongBaoResponse(ThongBao tb) {
        this.id = tb.getId();
        this.loai = tb.getLoai() != null ? tb.getLoai().name() : null;
        this.tieuDe = tb.getTieuDe();
        this.noiDung = tb.getNoiDung();
        this.link = tb.getLink();
        this.idThamChieu = tb.getIdThamChieu();
        this.maThamChieu = tb.getMaThamChieu();
        this.daDoc = tb.getDaDoc();
        this.ngayTao = tb.getThoiGian();
    }
}
