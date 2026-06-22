package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.idPhieuGiamGia IS NOT NULL")
    long countVoucherUsage();

    @Query("SELECT COALESCE(SUM(h.tienGiamGia), 0) FROM HoaDon h WHERE h.idPhieuGiamGia IS NOT NULL")
    BigDecimal sumVoucherSavings();
}
