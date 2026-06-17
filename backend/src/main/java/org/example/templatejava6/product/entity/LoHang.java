package org.example.templatejava6.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lo_hang")
public class LoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_san_pham")
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "so_lo", length = 40, nullable = false)
    private String soLo;

    @Column(name = "ngay_nhap", nullable = false)
    private LocalDate ngayNhap;

    @Column(name = "han_su_dung")
    private LocalDate hanSuDung;

    @Column(name = "so_luong_nhap", nullable = false)
    private Integer soLuongNhap;

    @Column(name = "so_luong_con", nullable = false)
    private Integer soLuongCon;

    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}
