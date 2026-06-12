package org.example.templatejava6.customer.repository;

import org.example.templatejava6.common.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    boolean existsByMaKhachHang(String maKhachHang);

    @Query("SELECT k FROM KhachHang k WHERE "
            + "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(k.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "k.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    List<KhachHang> timKiem(@Param("keyword") String keyword);

    @Query("SELECT k FROM KhachHang k WHERE "
            + "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(k.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "k.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    Page<KhachHang> timKiem(@Param("keyword") String keyword, Pageable pageable);
}
