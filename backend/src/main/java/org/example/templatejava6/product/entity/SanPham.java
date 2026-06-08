package org.example.templatejava6.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.category.entity.DangSanPham;
import org.example.templatejava6.category.entity.DanhMuc;
import org.example.templatejava6.category.entity.ThuongHieu;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 30)
    @Column(name = "ma_san_pham", length = 30)
    private String maSanPham;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten", length = 200)
    private String ten;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thuong_hieu")
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dang_san_pham")
    private DangSanPham dangSanPham;

    @Size(max = 10)
    @Column(name = "chi_so_spf", length = 10)
    private String chiSoSpf;

    @Size(max = 6)
    @Column(name = "chi_so_pa", length = 6)
    private String chiSoPa;

    @Size(max = 10)
    @Column(name = "loai_chong_nang", length = 10)
    private String loaiChongNang;

    @Column(name = "khang_nuoc")
    private Boolean khangNuoc;

    @Nationalized
    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
