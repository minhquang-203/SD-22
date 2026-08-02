package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.AnhHoanTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnhHoanTienRepository extends JpaRepository<AnhHoanTien, Integer> {

    List<AnhHoanTien> findByIdHoanTien_IdOrderByIdAsc(Integer idHoanTien);
}
