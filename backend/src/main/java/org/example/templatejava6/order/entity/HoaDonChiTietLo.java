package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.LoHang;

@Getter
@Setter
@Entity
@Table(name = "hoa_don_chi_tiet_lo")
public class HoaDonChiTietLo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don_chi_tiet", nullable = false)
    private HoaDonChiTiet hoaDonChiTiet;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lo_hang", nullable = false)
    private LoHang loHang;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
}
