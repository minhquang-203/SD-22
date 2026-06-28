package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Integer> {

    List<PhuongThucThanhToan> findByTrangThaiTrue();

    Optional<PhuongThucThanhToan> findByMaIgnoreCase(String ma);
}