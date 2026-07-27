package org.example.templatejava6.quiz.repository;

import org.example.templatejava6.quiz.entity.RoutineCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineComboRepository extends JpaRepository<RoutineCombo, Integer> {
    List<RoutineCombo> findByTrangThaiTrueOrderByThuTuAsc();
    List<RoutineCombo> findByLoaiDa_IdAndTrangThaiTrue(Integer loaiDaId);
}
