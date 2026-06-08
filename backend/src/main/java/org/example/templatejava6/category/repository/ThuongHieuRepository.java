package org.example.templatejava6.category.repository;

import org.example.templatejava6.category.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {

    List<ThuongHieu> findByTrangThaiTrue();

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);
}
