package org.example.templatejava6.product.repository;

import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnhSanPhamRepository extends JpaRepository<AnhSanPham, Integer> {

    /** JOIN FETCH mauSac để FE luôn nhận idMauSac (ảnh theo màu / dùng chung). */
    @Query("""
            SELECT a FROM AnhSanPham a
            LEFT JOIN FETCH a.mauSac
            WHERE a.sanPham = :sanPham
            ORDER BY a.thuTu ASC, a.id ASC
            """)
    List<AnhSanPham> findBySanPhamOrderByThuTuAsc(@Param("sanPham") SanPham sanPham);

    void deleteBySanPham(SanPham sanPham);

    Optional<AnhSanPham> findFirstBySanPham_IdAndLaAnhChinhTrue(Integer sanPhamId);

    Optional<AnhSanPham> findFirstBySanPham_IdOrderByThuTuAsc(Integer sanPhamId);

    List<AnhSanPham> findBySanPham_IdIn(Collection<Integer> sanPhamIds);
}
