package org.example.templatejava6.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.templatejava6.product.entity.SanPham;

@Entity
@Table(name = "routine_combo_chi_tiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineComboChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_routine", nullable = false)
    private RoutineCombo routine;

    @ManyToOne
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "thu_tu")
    private Integer thuTu = 0;

    @Column(name = "ghi_chu", length = 200)
    private String ghiChu;
}
