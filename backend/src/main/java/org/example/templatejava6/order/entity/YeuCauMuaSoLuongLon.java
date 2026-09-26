package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.product.entity.ChiTietSanPham;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "yeu_cau_mua_so_luong_lon")
public class YeuCauMuaSoLuongLon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham idChiTietSanPham;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Size(max = 200)
    @Column(name = "ten_cong_ty", length = 200)
    private String tenCongTy;

    @NotNull
    @Size(max = 100)
    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @NotNull
    @Size(max = 20)
    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @NotNull
    @Size(max = 100)
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 1000)
    @Column(name = "ghi_chu", length = 1000)
    private String ghiChu;

    @NotNull
    @Column(name = "nhan_khuyen_mai", nullable = false)
    private Boolean nhanKhuyenMai = false;

    @NotNull
    @Size(max = 20)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "MOI";

    @NotNull
    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Size(max = 1000)
    @Column(name = "ghi_chu_noi_bo", length = 1000)
    private String ghiChuNoiBo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_xu_ly")
    private NhanVien idNhanVienXuLy;

    @PrePersist
    void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (trangThai == null || trangThai.isBlank()) {
            trangThai = "MOI";
        }
        if (nhanKhuyenMai == null) {
            nhanKhuyenMai = false;
        }
    }
}
