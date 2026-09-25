package org.example.templatejava6.nhaphang.repository;

import org.example.templatejava6.nhaphang.entity.PhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PhieuNhapRepository extends JpaRepository<PhieuNhap, Integer> {

    @Query("""
            SELECT DISTINCT p FROM PhieuNhap p
            LEFT JOIN FETCH p.nhaCungCap
            LEFT JOIN FETCH p.nhanVien
            WHERE (:trangThai IS NULL OR :trangThai = '' OR p.trangThai = :trangThai)
              AND (:idNcc IS NULL OR p.nhaCungCap.id = :idNcc)
              AND (:from IS NULL OR p.ngayTao >= :from)
              AND (:to IS NULL OR p.ngayTao <= :to)
              AND (
                :q IS NULL OR :q = ''
                OR LOWER(p.maPhieuNhap) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(p.soHoaDonDauVao, '')) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            ORDER BY p.ngayTao DESC
            """)
    List<PhieuNhap> search(
            @Param("trangThai") String trangThai,
            @Param("idNcc") Integer idNcc,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("q") String q);

    @Query("""
            SELECT p FROM PhieuNhap p
            LEFT JOIN FETCH p.nhaCungCap
            LEFT JOIN FETCH p.nhanVien
            LEFT JOIN FETCH p.chiTiets ct
            LEFT JOIN FETCH ct.chiTietSanPham cts
            LEFT JOIN FETCH cts.sanPham
            LEFT JOIN FETCH ct.loHang
            WHERE p.id = :id
            """)
    Optional<PhieuNhap> findDetailById(@Param("id") Integer id);

    @Query("SELECT p.maPhieuNhap FROM PhieuNhap p")
    List<String> findAllMa();
}
