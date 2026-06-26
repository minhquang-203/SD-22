package org.example.templatejava6.cart.repository;

import org.example.templatejava6.cart.entity.ChiTietGioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Integer> {

    @Query("SELECT ctgh FROM ChiTietGioHang ctgh "
            + "JOIN FETCH ctgh.gioHang gh "
            + "JOIN FETCH ctgh.chiTietSanPham ctsp "
            + "JOIN FETCH ctsp.sanPham sp "
            + "LEFT JOIN FETCH ctsp.mauSac ms "
            + "WHERE gh.id = :idGioHang ORDER BY ctgh.id ASC")
    List<ChiTietGioHang> findByGioHangIdWithDetail(@Param("idGioHang") Integer idGioHang);

    Optional<ChiTietGioHang> findByGioHang_IdAndChiTietSanPham_Id(Integer idGioHang, Integer idChiTietSanPham);

    Optional<ChiTietGioHang> findByIdAndGioHang_KhachHang_Id(Integer id, Integer idKhachHang);

    void deleteByGioHang_Id(Integer idGioHang);
}
