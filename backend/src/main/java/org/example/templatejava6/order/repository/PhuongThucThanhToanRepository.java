package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Integer> {

    List<PhuongThucThanhToan> findByTrangThaiTrue();
}