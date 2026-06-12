package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "thanh_toan_hoa_don")
public class ThanhToanHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phuong_thuc_thanh_toan", nullable = false)
    private PhuongThucThanhToan idPhuongThucThanhToan;

    @NotNull
    @Column(name = "so_tien", nullable = false, precision = 12)
    private BigDecimal soTien;

    @Column(name = "so_tien_khach_dua", precision = 12)
    private BigDecimal soTienKhachDua;

    @Column(name = "tien_thua", precision = 12)
    private BigDecimal tienThua;

    @Size(max = 100)
    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich;

    @Size(max = 15)
    @ColumnDefault("'THANH_CONG'")
    @Column(name = "trang_thai", length = 15)
    private String trangThai;

    @ColumnDefault("getdate()")
    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

}