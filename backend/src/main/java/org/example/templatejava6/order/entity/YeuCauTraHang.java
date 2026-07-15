package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "yeu_cau_tra_hang")
public class YeuCauTraHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ly_do")
    private String lyDo;

    @Size(max = 500)
    @Nationalized
    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'CHO_DUYET'")
    @Column(name = "trang_thai", length = 20)
    private TrangThaiTraHang trangThai;

    @Size(max = 50)
    @Column(name = "ma_van_don_tra", length = 50)
    private String maVanDonTra;

    @Column(name = "ghn_district_id")
    private Integer ghnDistrictId;

    @Size(max = 20)
    @Column(name = "ghn_ward_code", length = 20)
    private String ghnWardCode;

    @Size(max = 255)
    @Nationalized
    @Column(name = "dia_chi_tra")
    private String diaChiTra;

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
    @Column(name = "ghi_chu_admin")
    private String ghiChuAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_duyet")
    private NhanVien idNhanVienDuyet;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
}
