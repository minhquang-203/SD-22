package org.example.templatejava6.quiz.repository;

import org.example.templatejava6.quiz.entity.KetQuaQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KetQuaQuizRepository extends JpaRepository<KetQuaQuiz, Integer> {
    Optional<KetQuaQuiz> findFirstByKhachHang_IdOrderByThoiGianDesc(Integer khachHangId);
}
