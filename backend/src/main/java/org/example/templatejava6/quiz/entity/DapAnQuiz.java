package org.example.templatejava6.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.templatejava6.common.entity.LoaiDa;  // Trỏ tới file LoaiDa.java trong common/entity của bạn

@Entity
@Table(name = "dap_an_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DapAnQuiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "icon")
    private String icon;

    @Column(name = "noi_dung", nullable = false)
    private String noiDung;

    @Column(name = "diem")
    private Integer diem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cau_hoi")
    private CauHoiQuiz cauHoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_da")
    private LoaiDa loaiDa;
}