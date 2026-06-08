package org.example.templatejava6.category.repository;

import org.example.templatejava6.category.entity.ThanhPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThanhPhanRepository extends JpaRepository<ThanhPhan, Integer> {

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);
}
