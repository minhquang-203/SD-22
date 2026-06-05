package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnhSanPhamRepository extends JpaRepository<AnhSanPham, Integer> {

    List<AnhSanPham> findBySanPhamOrderByThuTuAsc(SanPham sanPham);

    void deleteBySanPham(SanPham sanPham);
}
