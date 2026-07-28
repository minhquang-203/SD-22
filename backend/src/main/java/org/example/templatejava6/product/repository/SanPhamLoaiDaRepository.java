package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.entity.SanPhamLoaiDa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamLoaiDaRepository extends JpaRepository<SanPhamLoaiDa, Integer> {

    List<SanPhamLoaiDa> findBySanPham(SanPham sanPham);

    void deleteBySanPham(SanPham sanPham);

    @Query("SELECT s FROM SanPhamLoaiDa s JOIN FETCH s.loaiDa JOIN FETCH s.sanPham")
    List<SanPhamLoaiDa> findAllWithLoaiDa();
}
