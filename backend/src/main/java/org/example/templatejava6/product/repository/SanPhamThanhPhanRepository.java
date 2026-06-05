package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.entity.SanPhamThanhPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamThanhPhanRepository extends JpaRepository<SanPhamThanhPhan, Integer> {

    List<SanPhamThanhPhan> findBySanPham(SanPham sanPham);

    void deleteBySanPham(SanPham sanPham);
}
