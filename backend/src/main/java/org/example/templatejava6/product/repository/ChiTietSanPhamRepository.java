package org.example.templatejava6.product.repository;

import jakarta.persistence.LockModeType;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.id = :id")
    Optional<ChiTietSanPham> findByIdForUpdate(@Param("id") Integer id);

    List<ChiTietSanPham> findBySanPhamAndTrangThaiTrue(SanPham sanPham);

    List<ChiTietSanPham> findBySanPham(SanPham sanPham);

    Optional<ChiTietSanPham> findBySku(String sku);

    boolean existsBySku(String sku);

    void deleteBySanPham(SanPham sanPham);

    interface VariantAgg {
        Integer getSpId();

        BigDecimal getGiaMin();

        BigDecimal getGiaMax();

        Long getTongTon();

        Long getSoBienThe();
    }

    @Query("SELECT c.sanPham.id AS spId, MIN(c.giaBan) AS giaMin, MAX(c.giaBan) AS giaMax, "
            + "COALESCE(SUM(c.soLuongTon), 0) AS tongTon, COUNT(c) AS soBienThe "
            + "FROM ChiTietSanPham c WHERE c.trangThai = true GROUP BY c.sanPham.id")
    List<VariantAgg> aggregateActiveVariants();

    @Query("SELECT c FROM ChiTietSanPham c JOIN FETCH c.sanPham sp LEFT JOIN FETCH c.mauSac ms "
            + "WHERE c.trangThai = true AND sp.trangThai = true AND ("
            + "LOWER(c.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ChiTietSanPham> timBienTheBan(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM ChiTietSanPham c JOIN FETCH c.sanPham sp LEFT JOIN FETCH c.mauSac ms "
            + "WHERE c.trangThai = true AND sp.trangThai = true AND ("
            + ":keyword = '' OR LOWER(c.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "ORDER BY sp.ten ASC, c.sku ASC")
    List<ChiTietSanPham> danhSachBienTheBan(@Param("keyword") String keyword, Pageable pageable);
}
