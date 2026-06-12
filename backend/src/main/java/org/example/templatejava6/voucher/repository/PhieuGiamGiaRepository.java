package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    Page<PhieuGiamGia> findByTrangThaiTrue(Pageable pageable);

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);

    Optional<PhieuGiamGia> findByMa(String ma);

    @Query("""
    SELECT v FROM PhieuGiamGia v
    WHERE v.trangThai = true
           AND (:keyword IS NULL OR :keyword = ''
           OR LOWER(v.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(v.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
           
           AND (:loai IS NULL OR v.loai = :loai)

    AND (
        :timeStatus IS NULL

        OR (:timeStatus = 'ACTIVE'
            AND v.ngayBatDau <= CURRENT_TIMESTAMP
            AND v.ngayKetThuc >= CURRENT_TIMESTAMP)

        OR (:timeStatus = 'UPCOMING'
            AND v.ngayBatDau > CURRENT_TIMESTAMP)

        OR (:timeStatus = 'EXPIRED'
            AND v.ngayKetThuc < CURRENT_TIMESTAMP)
    )
""")
    Page<PhieuGiamGia> search(
            @Param("keyword") String keyword,
            @Param("timeStatus") String timeStatus,
            @Param("loai") LoaiPhieuGiamGia loai, Pageable pageable
    );
}
