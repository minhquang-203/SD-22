package org.example.templatejava6.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.templatejava6.common.entity.LoaiDa;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "routine_combo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineCombo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten", nullable = false, length = 200)
    private String ten;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "id_loai_da")
    private LoaiDa loaiDa;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @Column(name = "thu_tu")
    private Integer thuTu = 0;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineComboChiTiet> chiTiets;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
