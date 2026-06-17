package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.LoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoHangRepository extends JpaRepository<LoHang, Integer> {

    @Query("""
            SELECT l FROM LoHang l
            WHERE l.chiTietSanPham.id = :idCts
            ORDER BY l.ngayNhap DESC, l.id DESC
            """)
    List<LoHang> findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(@Param("idCts") Integer idChiTietSanPham);

    @Query("""
            SELECT l FROM LoHang l
            WHERE l.chiTietSanPham.id = :idCts
              AND l.trangThai = true
              AND l.soLuongCon > 0
            ORDER BY CASE WHEN l.hanSuDung IS NULL THEN 1 ELSE 0 END, l.hanSuDung ASC, l.id ASC
            """)
    List<LoHang> findAvailableForFefo(@Param("idCts") Integer idChiTietSanPham);

    @Query("""
            SELECT COALESCE(SUM(l.soLuongCon), 0) FROM LoHang l
            WHERE l.chiTietSanPham.id = :idCts AND l.trangThai = true
            """)
    int sumSoLuongCon(@Param("idCts") Integer idChiTietSanPham);
}
