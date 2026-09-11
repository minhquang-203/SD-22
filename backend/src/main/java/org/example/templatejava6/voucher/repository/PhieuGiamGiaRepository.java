package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    Page<PhieuGiamGia> findByTrangThaiTrue(Pageable pageable);

    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(String ma, Integer id);

    Optional<PhieuGiamGia> findByMa(String ma);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PhieuGiamGia v
        SET v.soLuong = v.soLuong - 1
        WHERE v.id = :id
          AND v.soLuong IS NOT NULL
          AND v.soLuong > 0
        """)
    int decrementSoLuongIfAvailable(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PhieuGiamGia v
        SET v.soLuong = COALESCE(v.soLuong, 0) + 1
        WHERE v.id = :id
        """)
    int incrementSoLuong(@Param("id") Integer id);

    @Query("""
    SELECT v FROM PhieuGiamGia v
    WHERE v.trangThai = true
           AND (:keyword IS NULL OR :keyword = ''
           OR LOWER(v.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(v.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
           AND (:loai IS NULL OR v.loai = :loai)
           AND (
               :timeStatus IS NULL
               OR (:timeStatus = 'INACTIVE' AND v.isActive = false)
               OR (v.isActive = true AND :timeStatus = 'ACTIVE'
                   AND v.ngayBatDau <= CURRENT_TIMESTAMP
                   AND v.ngayKetThuc >= CURRENT_TIMESTAMP)
               OR (v.isActive = true AND :timeStatus = 'UPCOMING'
                   AND v.ngayBatDau > CURRENT_TIMESTAMP)
               OR (v.isActive = true AND :timeStatus = 'EXPIRED'
                   AND v.ngayKetThuc < CURRENT_TIMESTAMP)
           )
""")
    Page<PhieuGiamGia> search(
            @Param("keyword") String keyword,
            @Param("timeStatus") String timeStatus,
            @Param("loai") LoaiPhieuGiamGia loai, Pageable pageable
    );

    @Query("""
        SELECT COUNT(v) FROM PhieuGiamGia v
        WHERE v.trangThai = true
        AND v.isActive = true
        AND v.ngayBatDau <= CURRENT_TIMESTAMP
        AND v.ngayKetThuc >= CURRENT_TIMESTAMP
    """)
    long countActive();

    @Query("""
        SELECT COUNT(v) FROM PhieuGiamGia v
        WHERE v.trangThai = true
        AND v.isActive = true
        AND v.ngayKetThuc >= CURRENT_TIMESTAMP
        AND v.ngayKetThuc <= :deadline
    """)
    long countExpiringSoon(@Param("deadline") LocalDateTime deadline);

    /** Voucher đang hiệu lực cho modal checkout — hiện tất cả; quyền chọn xử lý ở service. */
    @Query("""
        SELECT v FROM PhieuGiamGia v
        WHERE v.trangThai = true
          AND v.isActive = true
          AND v.soLuong > 0
          AND v.ngayBatDau <= CURRENT_TIMESTAMP
          AND v.ngayKetThuc >= CURRENT_TIMESTAMP
          AND (:keyword IS NULL OR :keyword = ''
              OR LOWER(v.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(v.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY v.ngayKetThuc ASC
        """)
    Page<PhieuGiamGia> findAvailableForCustomer(
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Mã tại quầy: không FREE_SHIP. Công khai luôn hiện;
     * mã cá nhân chỉ khi đã gán cho khách đang chọn trên POS.
     */
    @Query("""
        SELECT v FROM PhieuGiamGia v
        WHERE v.trangThai = true
          AND v.isActive = true
          AND v.soLuong > 0
          AND v.ngayBatDau <= CURRENT_TIMESTAMP
          AND v.ngayKetThuc >= CURRENT_TIMESTAMP
          AND v.loai <> org.example.templatejava6.common.enums.LoaiPhieuGiamGia.FREE_SHIP
          AND (:keyword IS NULL OR :keyword = ''
              OR LOWER(v.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(v.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (
                v.phamVi IS NULL
                OR v.phamVi = org.example.templatejava6.common.enums.PhamViPhieuGiamGia.CONG_KHAI
                OR (
                    v.phamVi = org.example.templatejava6.common.enums.PhamViPhieuGiamGia.CA_NHAN
                    AND :idKhachHang IS NOT NULL
                    AND EXISTS (
                        SELECT 1 FROM KhachHangPhieuGiamGia k
                        WHERE k.phieuGiamGia.id = v.id
                          AND k.khachHang.id = :idKhachHang
                    )
                )
              )
        ORDER BY v.ngayKetThuc ASC
        """)
    Page<PhieuGiamGia> findAvailableForPos(
            @Param("keyword") String keyword,
            @Param("idKhachHang") Integer idKhachHang,
            Pageable pageable);

    /**
     * Voucher công khai đang hiệu lực - dùng cho tab "Tất cả voucher" ở tài khoản.
     * phamVi NULL coi như CONG_KHAI (dữ liệu seed cũ trước khi có cột phạm vi).
     */
    @Query("""
        SELECT v FROM PhieuGiamGia v
        WHERE v.trangThai = true
          AND v.isActive = true
          AND v.soLuong > 0
          AND (
                v.phamVi IS NULL
                OR v.phamVi = org.example.templatejava6.common.enums.PhamViPhieuGiamGia.CONG_KHAI
              )
          AND v.ngayBatDau <= CURRENT_TIMESTAMP
          AND v.ngayKetThuc >= CURRENT_TIMESTAMP
          AND (:keyword IS NULL OR :keyword = ''
              OR LOWER(v.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(v.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY v.ngayKetThuc ASC
        """)
    Page<PhieuGiamGia> findPublicAvailableForCustomer(
            @Param("keyword") String keyword,
            Pageable pageable);
}
