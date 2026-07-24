package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThanhToanHoaDonRepository extends JpaRepository<ThanhToanHoaDon, Integer> {

    List<ThanhToanHoaDon> findByIdHoaDonOrderByThoiGianDesc(HoaDon hoaDon);

    List<ThanhToanHoaDon> findByIdHoaDonAndTrangThaiOrderByThoiGianDesc(HoaDon hoaDon, String trangThai);

    Optional<ThanhToanHoaDon> findByMaGiaoDich(String maGiaoDich);

    void deleteByIdHoaDon(HoaDon hoaDon);

    default Optional<ThanhToanHoaDon> findLatestByHoaDon(HoaDon hoaDon) {
        List<ThanhToanHoaDon> list = findByIdHoaDonOrderByThoiGianDesc(hoaDon);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    default Optional<ThanhToanHoaDon> findLatestByHoaDonAndTrangThai(HoaDon hoaDon, String trangThai) {
        List<ThanhToanHoaDon> list = findByIdHoaDonAndTrangThaiOrderByThoiGianDesc(hoaDon, trangThai);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
