package org.example.templatejava6.voucher.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * Quan hệ gán voucher cá nhân cho khách hàng.
 * Cả gán theo cá nhân lẫn gán hàng loạt theo nhóm đều lưu về bảng này.
 */
@Getter
@Setter
@Entity
@Table(name = "khach_hang_phieu_giam_gia",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_khpgg",
                columnNames = {"id_khach_hang", "id_phieu_giam_gia"}))
public class KhachHangPhieuGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phieu_giam_gia", nullable = false)
    private PhieuGiamGia phieuGiamGia;

    @ColumnDefault("0")
    @Column(name = "da_su_dung")
    private Boolean daSuDung;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_gan")
    private LocalDateTime ngayGan;
}
