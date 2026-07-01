package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotNull
    @Column(name = "ma_hoa_don", nullable = false, length = 30)
    private String maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phuong_thuc_thanh_toan")
    private PhuongThucThanhToan idPhuongThucThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia idPhieuGiamGia;

    @Size(max = 10)
    @ColumnDefault("'ONLINE'")
    @Column(name = "loai_don", length = 10)
    private String loaiDon;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'CHO_XAC_NHAN'")
    @Column(name = "trang_thai", length = 15)
    private TrangThaiDonHang trangThai;

    @Size(max = 255)
    @Nationalized
    @Column(name = "dia_chi_giao")
    private String diaChiGiao;

    @ColumnDefault("0")
    @Column(name = "tong_tien", precision = 12)
    private BigDecimal tongTien;

    @ColumnDefault("0")
    @Column(name = "tien_giam_gia", precision = 12)
    private BigDecimal tienGiamGia;

    @ColumnDefault("0")
    @Column(name = "phi_van_chuyen", precision = 12)
    private BigDecimal phiVanChuyen;

    @Size(max = 50)
    @Column(name = "ma_van_don_ghn", length = 50)
    private String maVanDonGhn;

    @Column(name = "ghn_district_id")
    private Integer ghnDistrictId;

    @Size(max = 20)
    @Column(name = "ghn_ward_code", length = 20)
    private String ghnWardCode;

    @ColumnDefault("0")
    @Column(name = "thanh_tien", precision = 12)
    private BigDecimal thanhTien;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;

    @Size(max = 15)
    @Column(name = "sdt_nguoi_nhan")
    private String sdtNguoiNhan;

    @OneToMany(mappedBy = "idHoaDon", fetch = FetchType.LAZY)
    private Set<LichSuDonHang> lichSuDonHangs = new LinkedHashSet<>();

}