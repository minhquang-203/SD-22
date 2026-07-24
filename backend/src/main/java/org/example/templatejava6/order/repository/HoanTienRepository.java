package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.enums.TrangThaiHoanTien;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoanTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoanTienRepository extends JpaRepository<HoanTien, Integer> {

    List<HoanTien> findAllByOrderByNgayTaoDesc();

    List<HoanTien> findByTrangThaiOrderByNgayTaoDesc(TrangThaiHoanTien trangThai);

    List<HoanTien> findByIdHoaDonOrderByNgayTaoDesc(HoaDon hoaDon);

    boolean existsByIdHoaDon_IdAndTrangThaiNot(Integer idHoaDon, TrangThaiHoanTien trangThai);
}
