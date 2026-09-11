package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.LichSuDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuDonHangRepository extends JpaRepository<LichSuDonHang, Integer> {

    List<LichSuDonHang> findAllByOrderByThoiGianDescIdDesc();

    List<LichSuDonHang> findByIdHoaDon_IdOrderByThoiGianDescIdDesc(Integer idHoaDon);

    void deleteByIdHoaDon_Id(Integer idHoaDon);
}