package org.example.templatejava6.common.repository;

import org.example.templatejava6.common.entity.LoaiDa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiDaRepository extends JpaRepository<LoaiDa, Integer> {
}
