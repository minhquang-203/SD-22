package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.HoaDonChiTietLo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonChiTietLoRepository extends JpaRepository<HoaDonChiTietLo, Integer> {

    List<HoaDonChiTietLo> findByHoaDonChiTiet_IdOrderByIdAsc(Integer idHoaDonChiTiet);

    List<HoaDonChiTietLo> findByHoaDonChiTiet(HoaDonChiTiet hoaDonChiTiet);

    void deleteByHoaDonChiTiet(HoaDonChiTiet hoaDonChiTiet);

    @Query("""
            SELECT r FROM HoaDonChiTietLo r
            JOIN FETCH r.loHang l
            JOIN FETCH r.hoaDonChiTiet ct
            JOIN FETCH ct.idChiTietSanPham cts
            LEFT JOIN FETCH cts.sanPham
            WHERE ct.idHoaDon = :hoaDon
            ORDER BY r.id ASC
            """)
    List<HoaDonChiTietLo> findByHoaDonFetchLo(@Param("hoaDon") HoaDon hoaDon);

    @Query("""
            SELECT r FROM HoaDonChiTietLo r
            WHERE r.hoaDonChiTiet.idHoaDon.id = :idHoaDon
            ORDER BY r.id ASC
            """)
    List<HoaDonChiTietLo> findByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);
}
