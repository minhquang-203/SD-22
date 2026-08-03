package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    List<SanPham> findByTrangThaiTrue();

    List<SanPham> findByTrangThaiTrue(Sort sort);

    Page<SanPham> findByTrangThaiTrue(Pageable pageable);

    List<SanPham> findByTenContainingIgnoreCaseAndTrangThaiTrue(String ten);

    List<SanPham> findByTenContainingIgnoreCaseAndTrangThaiTrue(String ten, Sort sort);

    Page<SanPham> findByTenContainingIgnoreCaseAndTrangThaiTrue(String ten, Pageable pageable);

    List<SanPham> findByTenContainingIgnoreCase(String ten);

    List<SanPham> findByTenContainingIgnoreCase(String ten, Sort sort);

    Page<SanPham> findByTenContainingIgnoreCase(String ten, Pageable pageable);

    boolean existsByMaSanPham(String maSanPham);

    boolean existsByMaSanPhamAndIdNot(String maSanPham, Integer id);
}
