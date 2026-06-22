package org.example.templatejava6.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "cau_hoi_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CauHoiQuiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "noi_dung", nullable = false)
    private String noiDung;

    @Column(name = "thu_tu")
    private Integer thuTu = 0;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @Column(name = "bat_buoc")
    private Boolean batBuoc = true;

    // CascadeType.ALL: Khi xóa câu hỏi, tự động xóa sạch các đáp án thuộc về nó
    @OneToMany(mappedBy = "cauHoi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DapAnQuiz> dapAns;
}