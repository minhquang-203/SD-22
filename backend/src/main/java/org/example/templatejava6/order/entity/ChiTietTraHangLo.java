package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.enums.LoaiHangTra;
import org.example.templatejava6.product.entity.LoHang;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_tra_hang_lo")
public class ChiTietTraHangLo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_yeu_cau_tra_hang", nullable = false)
    private YeuCauTraHang yeuCauTraHang;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lo_hang", nullable = false)
    private LoHang loHang;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 10)
    private LoaiHangTra loai;
}
