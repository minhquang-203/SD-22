package org.example.templatejava6.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.templatejava6.common.entity.LoaiDa;
import org.example.templatejava6.common.entity.KhachHang;

import java.time.LocalDateTime;

@Entity
@Table(name = "ket_qua_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_da_ket_qua", nullable = false)
    private LoaiDa loaiDaKetQua;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @PrePersist
    protected void onCreate() {
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }
}
