package org.example.templatejava6.quiz.repository;

import org.example.templatejava6.quiz.entity.DapAnQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DapAnQuizRepository extends JpaRepository<DapAnQuiz, Integer> {
}