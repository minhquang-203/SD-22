package org.example.templatejava6.customer.entity;

import jakarta.persistence.*;
import org.example.templatejava6.common.entity.KhachHang;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @Size(max = 100)
    @Column(name = "ho_ten_nguoi_nhan", length = 100)
    private String hoTenNguoiNhan;

    @Size(max = 15)
    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Size(max = 50)
    @Column(name = "tinh_thanh", length = 50)
    private String tinhThanh;

    @Size(max = 50)
    @Column(name = "quan_huyen", length = 50)
    private String quanHuyen;

    @Size(max = 50)
    @Column(name = "phuong_xa", length = 50)
    private String phuongXa;
    @Column(name = "dia_chi_chi_tiet", length = 255)
    private String diaChiChiTiet;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "district_id")
    private Integer districtId;

    @Size(max = 20)
    @Column(name = "ward_code", length = 20)
    private String wardCode;

    @Column(name = "mac_dinh")
    private Boolean macDinh;
}
