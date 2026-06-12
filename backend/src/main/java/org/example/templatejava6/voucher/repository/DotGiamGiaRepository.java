package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {

    List<DotGiamGia> findByTrangThaiTrue();

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);
}
