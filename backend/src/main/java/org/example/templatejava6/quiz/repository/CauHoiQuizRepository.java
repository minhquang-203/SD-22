package org.example.templatejava6.quiz.repository;

import org.example.templatejava6.quiz.entity.CauHoiQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CauHoiQuizRepository extends JpaRepository<CauHoiQuiz, Integer> {
    List<CauHoiQuiz> findByTrangThaiTrueOrderByThuTuAsc();
}