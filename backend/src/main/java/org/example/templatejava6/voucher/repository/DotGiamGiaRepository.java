package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {

    List<DotGiamGia> findByTrangThaiTrue();

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);

    @Query("""
            SELECT d FROM DotGiamGia d
            WHERE d.trangThai = true
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(d.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (
                   :timeStatus IS NULL OR :timeStatus = ''
                   OR (:timeStatus = 'INACTIVE' AND d.isActive = false)
                   OR (d.isActive = true AND :timeStatus = 'ACTIVE'
                       AND d.ngayBatDau <= CURRENT_TIMESTAMP
                       AND d.ngayKetThuc >= CURRENT_TIMESTAMP)
                   OR (d.isActive = true AND :timeStatus = 'UPCOMING'
                       AND d.ngayBatDau > CURRENT_TIMESTAMP)
                   OR (d.isActive = true AND :timeStatus = 'EXPIRED'
                       AND d.ngayKetThuc < CURRENT_TIMESTAMP)
              )
            """)
    Page<DotGiamGia> search(
            @Param("keyword") String keyword,
            @Param("timeStatus") String timeStatus,
            Pageable pageable);
}
