package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    List<ChiTietSanPham> findBySanPhamAndTrangThaiTrue(SanPham sanPham);

    List<ChiTietSanPham> findBySanPham(SanPham sanPham);

    Optional<ChiTietSanPham> findBySku(String sku);

    boolean existsBySku(String sku);

    void deleteBySanPham(SanPham sanPham);
}
