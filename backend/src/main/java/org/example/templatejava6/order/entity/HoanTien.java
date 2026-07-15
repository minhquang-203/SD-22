package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.enums.LoaiHoanTien;
import org.example.templatejava6.common.enums.TrangThaiHoanTien;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hoan_tien")
public class HoanTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_yeu_cau_tra_hang")
    private YeuCauTraHang idYeuCauTraHang;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", length = 10)
    private LoaiHoanTien loai;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "so_tien", nullable = false, precision = 12)
    private BigDecimal soTien;

    @Size(max = 20)
    @Column(name = "phuong_thuc", length = 20)
    private String phuongThuc;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'CHO_XU_LY'")
    @Column(name = "trang_thai", length = 15)
    private TrangThaiHoanTien trangThai;

    @Size(max = 100)
    @Column(name = "ma_giao_dich_hoan", length = 100)
    private String maGiaoDichHoan;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_ngan_hang", length = 100)
    private String tenNganHang;

    @Size(max = 30)
    @Column(name = "so_tai_khoan", length = 30)
    private String soTaiKhoan;

    @Size(max = 100)
    @Nationalized
    @Column(name = "chu_tai_khoan", length = 100)
    private String chuTaiKhoan;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    /** Raw response nha cung cap khi goi API hoan tien (doi soat). */
    @Size(max = 500)
    @Nationalized
    @Column(name = "phan_hoi_ncc", length = 500)
    private String phanHoiNcc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_hoan")
    private LocalDateTime ngayHoan;
}
