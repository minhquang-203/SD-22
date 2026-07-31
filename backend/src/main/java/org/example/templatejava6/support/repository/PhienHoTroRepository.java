package org.example.templatejava6.support.repository;

import org.example.templatejava6.support.entity.PhienHoTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhienHoTroRepository extends JpaRepository<PhienHoTro, Integer> {

    Optional<PhienHoTro> findFirstByIdKhachHang_IdAndTrangThaiOrderByCapNhatCuoiDesc(
            Integer idKhachHang, String trangThai);

    @Query("""
            SELECT p FROM PhienHoTro p
            LEFT JOIN FETCH p.idKhachHang
            LEFT JOIN FETCH p.nguoiXuLy
            WHERE p.trangThai = :trangThai
            ORDER BY p.capNhatCuoi DESC
            """)
    List<PhienHoTro> findOpenSessions(@Param("trangThai") String trangThai);
}
