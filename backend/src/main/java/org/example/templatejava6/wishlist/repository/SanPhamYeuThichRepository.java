package org.example.templatejava6.wishlist.repository;

import org.example.templatejava6.wishlist.entity.SanPhamYeuThich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamYeuThichRepository extends JpaRepository<SanPhamYeuThich, Integer> {

    List<SanPhamYeuThich> findByIdKhachHang(Integer idKhachHang);

    Optional<SanPhamYeuThich> findByIdKhachHangAndSanPham_Id(Integer idKhachHang, Integer idSanPham);

    boolean existsByIdKhachHangAndSanPham_Id(Integer idKhachHang, Integer idSanPham);
}
