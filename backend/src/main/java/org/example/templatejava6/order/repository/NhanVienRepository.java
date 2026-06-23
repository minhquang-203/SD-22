package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    List<NhanVien> findByTrangThaiTrue();

    @Query("SELECT n FROM NhanVien n WHERE LOWER(n.email) = LOWER(:taiKhoan) OR n.soDienThoai = :taiKhoan")
    Optional<NhanVien> findByEmailOrSoDienThoai(@Param("taiKhoan") String taiKhoan);
}