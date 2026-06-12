package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    boolean existsByMaHoaDon(String maHoaDon);

    boolean existsByMaHoaDonAndIdNot(String maHoaDon, Integer id);

    List<HoaDon> findByMaHoaDonContainingIgnoreCase(String keyword);

    Page<HoaDon> findAllByOrderByNgayTaoDesc(Pageable pageable);

    List<HoaDon> findAllByOrderByNgayTaoDesc();

    List<HoaDon> findByTrangThaiAndLoaiDonOrderByNgayTaoDesc(TrangThaiDonHang trangThai, String loaiDon);

    Optional<HoaDon> findByIdAndTrangThaiAndLoaiDon(Integer id, TrangThaiDonHang trangThai, String loaiDon);
}
