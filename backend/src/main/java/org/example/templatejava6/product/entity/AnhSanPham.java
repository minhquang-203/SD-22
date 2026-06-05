package org.example.templatejava6.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "anh_san_pham")
public class AnhSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "la_anh_chinh")
    private Boolean laAnhChinh;

    @Column(name = "thu_tu")
    private Integer thuTu;
}
