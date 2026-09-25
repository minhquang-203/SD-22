package org.example.templatejava6.nhaphang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "nha_cung_cap")
public class NhaCungCap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma", length = 20, nullable = false, unique = true)
    private String ma;
    @Column(name = "ten", length = 200, nullable = false)
    private String ten;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;
    @Column(name = "dia_chi", length = 255)
    private String diaChi;
    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;
}
