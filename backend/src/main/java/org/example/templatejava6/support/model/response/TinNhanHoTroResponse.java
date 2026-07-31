package org.example.templatejava6.support.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.support.entity.PhienHoTro;
import org.example.templatejava6.support.entity.TinNhanHoTro;

import java.time.LocalDateTime;

@Getter
@Setter
public class TinNhanHoTroResponse {
    private Integer id;
    private Integer idPhien;
    private String nguoiGui;
    private Integer idNguoiGui;
    private String noiDung;
    private Boolean daDoc;
    private LocalDateTime thoiGian;

    public static TinNhanHoTroResponse from(TinNhanHoTro tin) {
        TinNhanHoTroResponse res = new TinNhanHoTroResponse();
        res.setId(tin.getId());
        res.setIdPhien(tin.getIdPhien() != null ? tin.getIdPhien().getId() : null);
        res.setNguoiGui(tin.getNguoiGui());
        res.setIdNguoiGui(tin.getIdNguoiGui());
        res.setNoiDung(tin.getNoiDung());
        res.setDaDoc(tin.getDaDoc());
        res.setThoiGian(tin.getThoiGian());
        return res;
    }
}
