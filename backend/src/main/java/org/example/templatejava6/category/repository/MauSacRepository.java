package org.example.templatejava6.category.repository;

import org.example.templatejava6.category.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);
}
