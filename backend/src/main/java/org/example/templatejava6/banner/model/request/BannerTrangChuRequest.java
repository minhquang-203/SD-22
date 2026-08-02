package org.example.templatejava6.banner.model.request;

import lombok.Data;

@Data
public class BannerTrangChuRequest {
    private String tieuDe;
    private String tieuDeChinh;
    private String moTa;
    private String nutText;
    private String linkUrl;
    private String anhUrl;
    private Integer thuTu;
    private Boolean trangThai;
}
