package org.example.templatejava6.Uv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "san_pham")
@Data
public class SanPhamUV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_san_pham")
    private String maSanPham;

    @Column(name = "ten")
    private String ten;

    @Column(name = "chi_so_spf")
    private String chiSoSpf;

    @Column(name = "noi_bat")
    private Boolean noiBat;

    // Chỉ định nghĩa mối quan hệ ở đây để JPA hiểu
    @ManyToMany
    @JoinTable(
            name = "san_pham_cong_dung",
            joinColumns = @JoinColumn(name = "id_san_pham"),
            inverseJoinColumns = @JoinColumn(name = "id_cong_dung")
    )
    private List<CongDungUv> congDungList; // Tên biến này phải khớp với repo

}
