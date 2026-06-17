package org.example.templatejava6.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.category.entity.MauSac;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @Size(max = 40)
    @Column(name = "sku", length = 40)
    private String sku;

    @Column(name = "dung_tich_ml", precision = 6, scale = 1)
    private BigDecimal dungTichMl;

    @Column(name = "gia_ban", precision = 12, scale = 0)
    private BigDecimal giaBan;

    @Column(name = "so_luong_ton")
    private Integer soLuongTon;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}
