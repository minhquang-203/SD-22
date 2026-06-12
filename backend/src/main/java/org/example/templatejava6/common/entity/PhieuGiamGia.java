package org.example.templatejava6.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotNull
    @Column(name = "ma", nullable = false, length = 30)
    private String ma;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten", length = 100)
    private String ten;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 10)
    private LoaiPhieuGiamGia loai;

    @NotNull
    @Column(name = "gia_tri", nullable = false, precision = 12)
    private BigDecimal giaTri;

    @ColumnDefault("0")
    @Column(name = "gia_tri_don_toi_thieu", precision = 12)
    private BigDecimal giaTriDonToiThieu;

    @Column(name = "giam_toi_da", precision = 12)
    private BigDecimal giamToiDa;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

}