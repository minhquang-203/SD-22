package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.HoaDonChiTietLo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonChiTietLoRepository extends JpaRepository<HoaDonChiTietLo, Integer> {

    List<HoaDonChiTietLo> findByHoaDonChiTiet_IdOrderByIdAsc(Integer idHoaDonChiTiet);

    List<HoaDonChiTietLo> findByHoaDonChiTiet(HoaDonChiTiet hoaDonChiTiet);

    void deleteByHoaDonChiTiet(HoaDonChiTiet hoaDonChiTiet);
}
