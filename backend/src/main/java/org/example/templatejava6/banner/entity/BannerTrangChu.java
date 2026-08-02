package org.example.templatejava6.banner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "banner_trang_chu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerTrangChu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tieu_de", length = 200)
    private String tieuDe;

    @Column(name = "tieu_de_chinh", nullable = false, length = 300)
    private String tieuDeChinh;

    @Column(name = "mo_ta", length = 1000)
    private String moTa;

    @Column(name = "nut_text", length = 100)
    private String nutText;

    @Column(name = "link_url", nullable = false, length = 500)
    private String linkUrl;

    @Column(name = "anh_url", length = 500)
    private String anhUrl;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;

    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = true;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (thuTu == null) {
            thuTu = 0;
        }
        if (trangThai == null) {
            trangThai = true;
        }
    }
}
