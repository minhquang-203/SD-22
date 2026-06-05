package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.entity.SanPhamCongDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamCongDungRepository extends JpaRepository<SanPhamCongDung, Integer> {

    List<SanPhamCongDung> findBySanPham(SanPham sanPham);

    void deleteBySanPham(SanPham sanPham);
}
