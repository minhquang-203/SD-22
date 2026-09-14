package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = true AND ("
            + ":keyword = '' OR LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR EXISTS (SELECT 1 FROM ChiTietSanPham c WHERE c.sanPham = sp AND c.trangThai = true "
            + "AND LOWER(c.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "ORDER BY sp.ten ASC")
    List<SanPham> timChoNhapHang(@Param("keyword") String keyword, Pageable pageable);

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
