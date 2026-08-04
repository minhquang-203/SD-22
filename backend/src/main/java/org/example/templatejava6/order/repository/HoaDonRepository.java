package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    boolean existsByMaHoaDon(String maHoaDon);

    boolean existsByMaHoaDonAndIdNot(String maHoaDon, Integer id);

    List<HoaDon> findByMaHoaDonContainingIgnoreCase(String keyword);

    Page<HoaDon> findAllByOrderByNgayTaoDesc(Pageable pageable);

    List<HoaDon> findAllByOrderByNgayTaoDesc();

    /**
     * Admin: ẩn đơn VNPAY chưa có ThanhToanHoaDon = THANH_CONG.
     * COD vẫn hiện dù payment còn CHO_THANH_TOAN.
     */
    @Query("""
            SELECT h FROM HoaDon h
            WHERE NOT (
                UPPER(h.idPhuongThucThanhToan.ma) = 'VNPAY'
                AND NOT EXISTS (
                    SELECT 1 FROM ThanhToanHoaDon t
                    WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
                )
            )
            ORDER BY h.ngayTao DESC
            """)
    List<HoaDon> findVisibleForAdminOrderByNgayTaoDesc();

    @Query("""
            SELECT h FROM HoaDon h
            WHERE NOT (
                UPPER(h.idPhuongThucThanhToan.ma) = 'VNPAY'
                AND NOT EXISTS (
                    SELECT 1 FROM ThanhToanHoaDon t
                    WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
                )
            )
            ORDER BY h.ngayTao DESC
            """)
    Page<HoaDon> findVisibleForAdminOrderByNgayTaoDesc(Pageable pageable);

    @Query("""
            SELECT h FROM HoaDon h
            WHERE LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
              AND NOT (
                UPPER(h.idPhuongThucThanhToan.ma) = 'VNPAY'
                AND NOT EXISTS (
                    SELECT 1 FROM ThanhToanHoaDon t
                    WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
                )
              )
            ORDER BY h.ngayTao DESC
            """)
    List<HoaDon> findVisibleForAdminByMaHoaDonContaining(@Param("keyword") String keyword);

    /**
     * Admin list: ẩn VNPAY chưa thanh toán + lọc keyword/loại/trạng thái/ngày + phân trang.
     */
    @Query(
            value = """
                    SELECT h FROM HoaDon h
                    LEFT JOIN h.idKhachHang kh
                    LEFT JOIN h.idNhanVien nv
                    LEFT JOIN h.idPhuongThucThanhToan pttt
                    WHERE NOT (
                        UPPER(pttt.ma) = 'VNPAY'
                        AND NOT EXISTS (
                            SELECT 1 FROM ThanhToanHoaDon t
                            WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
                        )
                    )
                      AND (:keyword IS NULL OR :keyword = ''
                           OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(nv.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(pttt.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:loaiDon IS NULL OR :loaiDon = '' OR UPPER(h.loaiDon) = UPPER(:loaiDon))
                      AND (:trangThai IS NULL OR h.trangThai = :trangThai)
                      AND (:from IS NULL OR h.ngayTao >= :from)
                      AND (:to IS NULL OR h.ngayTao <= :to)
                    """,
            countQuery = """
                    SELECT COUNT(h) FROM HoaDon h
                    LEFT JOIN h.idKhachHang kh
                    LEFT JOIN h.idNhanVien nv
                    LEFT JOIN h.idPhuongThucThanhToan pttt
                    WHERE NOT (
                        UPPER(pttt.ma) = 'VNPAY'
                        AND NOT EXISTS (
                            SELECT 1 FROM ThanhToanHoaDon t
                            WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
                        )
                    )
                      AND (:keyword IS NULL OR :keyword = ''
                           OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(nv.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(pttt.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:loaiDon IS NULL OR :loaiDon = '' OR UPPER(h.loaiDon) = UPPER(:loaiDon))
                      AND (:trangThai IS NULL OR h.trangThai = :trangThai)
                      AND (:from IS NULL OR h.ngayTao >= :from)
                      AND (:to IS NULL OR h.ngayTao <= :to)
                    """)
    Page<HoaDon> searchVisibleForAdmin(
            @Param("keyword") String keyword,
            @Param("loaiDon") String loaiDon,
            @Param("trangThai") TrangThaiDonHang trangThai,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("""
            SELECT COUNT(h) FROM HoaDon h
            LEFT JOIN h.idPhuongThucThanhToan pttt
            WHERE NOT (
                UPPER(pttt.ma) = 'VNPAY'
                AND NOT EXISTS (
                    SELECT 1 FROM ThanhToanHoaDon t
                    WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
                )
            )
              AND (:trangThai IS NULL OR h.trangThai = :trangThai)
            """)
    long countVisibleForAdmin(@Param("trangThai") TrangThaiDonHang trangThai);

    List<HoaDon> findByTrangThaiAndLoaiDonOrderByNgayTaoDesc(TrangThaiDonHang trangThai, String loaiDon);

    long countByTrangThaiAndLoaiDon(TrangThaiDonHang trangThai, String loaiDon);

    Optional<HoaDon> findByIdAndTrangThaiAndLoaiDon(Integer id, TrangThaiDonHang trangThai, String loaiDon);

    List<HoaDon> findByIdKhachHang_IdAndLoaiDonOrderByNgayTaoDesc(Integer idKhachHang, String loaiDon);

    List<HoaDon> findByIdKhachHang_IdOrderByNgayTaoDesc(Integer idKhachHang);

    Optional<HoaDon> findByIdAndIdKhachHang_Id(Integer id, Integer idKhachHang);

    Optional<HoaDon> findByIdAndIdKhachHang_IdAndLoaiDon(Integer id, Integer idKhachHang, String loaiDon);

    List<HoaDon> findByMaVanDonGhnNotNullAndTrangThaiNotIn(java.util.Collection<TrangThaiDonHang> trangThaiKetThuc);

    @Query("""
            SELECT h FROM HoaDon h
            WHERE h.loaiDon = 'ONLINE'
              AND h.trangThai = org.example.templatejava6.common.enums.TrangThaiDonHang.CHO_XAC_NHAN
              AND h.ngayTao <= :cutoff
              AND UPPER(h.idPhuongThucThanhToan.ma) = 'VNPAY'
              AND NOT EXISTS (
                  SELECT 1 FROM ThanhToanHoaDon t
                  WHERE t.idHoaDon = h AND t.trangThai = 'THANH_CONG'
              )
            ORDER BY h.ngayTao ASC
            """)
    List<HoaDon> findExpiredUnpaidVnpayOrders(LocalDateTime cutoff);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.idPhieuGiamGia IS NOT NULL")
    long countVoucherUsage();

    @Query("SELECT COALESCE(SUM(h.tienGiamGia), 0) FROM HoaDon h WHERE h.idPhieuGiamGia IS NOT NULL")
    BigDecimal sumVoucherSavings();
}
