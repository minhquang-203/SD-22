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

    List<HoaDon> findByTrangThaiAndLoaiDonOrderByNgayTaoDesc(TrangThaiDonHang trangThai, String loaiDon);

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
