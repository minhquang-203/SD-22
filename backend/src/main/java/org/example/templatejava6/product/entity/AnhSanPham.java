package org.example.templatejava6.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.templatejava6.category.entity.MauSac;

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

    /** NULL = ảnh chung sản phẩm; có giá trị = ảnh gắn biến thể cụ thể */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_san_pham")
    private ChiTietSanPham chiTietSanPham;

    /** NULL = ảnh dùng chung mọi màu; có giá trị = ảnh riêng của màu đó */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "la_anh_chinh")
    private Boolean laAnhChinh;

    @Column(name = "thu_tu")
    private Integer thuTu;
}
