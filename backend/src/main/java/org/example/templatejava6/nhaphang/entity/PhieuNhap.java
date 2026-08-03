package org.example.templatejava6.nhaphang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.NhanVien;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "phieu_nhap")
public class PhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_phieu_nhap", length = 20, nullable = false, unique = true)
    private String maPhieuNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nha_cung_cap")
    private NhaCungCap nhaCungCap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "so_hoa_don_dau_vao", length = 50)
    private String soHoaDonDauVao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Column(name = "giam_gia", precision = 18, scale = 2)
    private BigDecimal giamGia = BigDecimal.ZERO;

    @Column(name = "can_tra_ncc", precision = 18, scale = 2)
    private BigDecimal canTraNcc = BigDecimal.ZERO;

    @Column(name = "trang_thai", length = 20, nullable = false)
    private String trangThai = "PHIEU_TAM";

    @Nationalized
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietPhieuNhap> chiTiets = new ArrayList<>();
}
