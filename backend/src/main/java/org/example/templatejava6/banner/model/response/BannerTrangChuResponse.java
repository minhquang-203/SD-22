package org.example.templatejava6.banner.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerTrangChuResponse {
    private Integer id;
    private String tieuDe;
    private String tieuDeChinh;
    private String moTa;
    private String nutText;
    private String linkUrl;
    private String anhUrl;
    private Integer thuTu;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
}
