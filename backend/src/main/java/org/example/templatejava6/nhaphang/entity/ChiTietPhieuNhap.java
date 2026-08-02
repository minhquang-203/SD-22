package org.example.templatejava6.nhaphang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.LoHang;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_phieu_nhap")
public class ChiTietPhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_nhap", nullable = false)
    private PhieuNhap phieuNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", precision = 18, scale = 2, nullable = false)
    private BigDecimal donGia = BigDecimal.ZERO;

    @Column(name = "han_su_dung")
    private LocalDate hanSuDung;

    @Column(name = "so_lo", length = 40)
    private String soLo;

    @Column(name = "thanh_tien", precision = 18, scale = 2)
    private BigDecimal thanhTien = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lo_hang")
    private LoHang loHang;
}
