package org.example.templatejava6.category.repository;

import org.example.templatejava6.category.entity.CongDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CongDungRepository extends JpaRepository<CongDung, Integer> {
}
