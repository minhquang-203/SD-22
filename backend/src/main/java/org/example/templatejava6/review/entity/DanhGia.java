package org.example.templatejava6.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.product.entity.SanPham;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "danh_gia")
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @Column(name = "so_sao")
    private Byte soSao;

    @Nationalized
    @Column(name = "noi_dung", length = 500)
    private String noiDung;

    @Column(name = "trang_thai", length = 15)
    private String trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
