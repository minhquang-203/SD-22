package org.example.templatejava6.statistic.repository;

import org.example.templatejava6.order.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface StatisticRepository extends JpaRepository<HoaDon, Integer> {

    // 1. TỔNG QUAN THỐNG KÊ (Đã cập nhật Doanh Thu Dự Kiến & Thực Tế)
    @Query(value = """
        SELECT 
            -- Index 0: Tổng doanh thu (Tất cả đơn không bị hủy)
            ISNULL(SUM(CASE WHEN trang_thai IN ('HOAN_THANH', 'CHO_XAC_NHAN', 'DANG_GIAO') THEN thanh_tien ELSE 0 END), 0) AS totalRevenue,
            
            -- Index 1: Doanh thu thực tế (Chỉ tính đơn đã HOAN_THANH)
            ISNULL(SUM(CASE WHEN trang_thai = 'HOAN_THANH' THEN thanh_tien ELSE 0 END), 0) AS actualRevenue,
            
            -- Index 2: Doanh thu dự kiến (Đơn CHO_XAC_NHAN, DANG_GIAO)
            ISNULL(SUM(CASE WHEN trang_thai IN ('CHO_XAC_NHAN', 'DANG_GIAO') THEN thanh_tien ELSE 0 END), 0) AS expectedRevenue,
            
            -- Index 3: Số đơn hàng (Không đếm đơn Hủy)
            SUM(CASE WHEN trang_thai IN ('HOAN_THANH', 'CHO_XAC_NHAN', 'DANG_GIAO') THEN 1 ELSE 0 END) AS totalOrders,
            
            -- Index 4: Đơn Online
            SUM(CASE WHEN loai_don = 'ONLINE' AND trang_thai IN ('HOAN_THANH', 'CHO_XAC_NHAN', 'DANG_GIAO') THEN 1 ELSE 0 END) AS webOrders,
            
            -- Index 5: Đơn Tại Quầy
            SUM(CASE WHEN loai_don = 'TAI_QUAY' AND trang_thai IN ('HOAN_THANH', 'CHO_XAC_NHAN', 'DANG_GIAO') THEN 1 ELSE 0 END) AS posOrders
            
        FROM hoa_don
        WHERE ngay_tao >= :fromDate AND ngay_tao <= :toDate
    """, nativeQuery = true)
    Object[] getTongQuanThongKe(@Param("fromDate") LocalDateTime fromDate,
                                @Param("toDate") LocalDateTime toDate);

    // 2. LẤY TOP 5 SẢN PHẨM BÁN CHẠY NHẤT THẬT
    @Query(value = """
        SELECT TOP 5 sp.ten AS name, ISNULL(SUM(hdct.so_luong), 0) AS value
        FROM hoa_don_chi_tiet hdct
        JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
        JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id
        JOIN san_pham sp ON ctsp.id_san_pham = sp.id
        WHERE hd.ngay_tao >= :fromDate AND hd.ngay_tao <= :toDate
        AND hd.trang_thai = 'HOAN_THANH'
        GROUP BY sp.id, sp.ten
        ORDER BY value DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getTopProducts(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    // 3. LẤY CƠ CẤU DÒNG TIỀN THẬT (Nhóm theo mã thanh toán)
    @Query(value = """
        SELECT pt.ma AS name, ISNULL(SUM(hd.thanh_tien), 0) AS value
        FROM hoa_don hd
        JOIN phuong_thuc_thanh_toan pt ON hd.id_phuong_thuc_thanh_toan = pt.id
        WHERE hd.ngay_tao >= :fromDate AND hd.ngay_tao <= :toDate
        AND hd.trang_thai = 'HOAN_THANH'
        GROUP BY pt.ma
    """, nativeQuery = true)
    List<Map<String, Object>> getPaymentFlow(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    // 4. LẤY DỮ LIỆU BIỂU ĐỒ THẬT (Gom nhóm theo Ngày)
    @Query(value = """
        SELECT FORMAT(ngay_tao, 'yyyy-MM-dd') AS name, ISNULL(SUM(thanh_tien), 0) AS value
        FROM hoa_don
        WHERE ngay_tao >= :fromDate AND ngay_tao <= :toDate
        AND trang_thai = 'HOAN_THANH'
        GROUP BY FORMAT(ngay_tao, 'yyyy-MM-dd')
        ORDER BY name ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getChartData(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    // 5. LẤY DỮ LIỆU KHUYẾN MÃI (VOUCHER) THẬT
    @Query(value = """
        SELECT 
            ISNULL(SUM(tien_giam_gia), 0) AS totalDiscount,
            SUM(CASE WHEN id_phieu_giam_gia IS NOT NULL THEN 1 ELSE 0 END) AS totalUsage
        FROM hoa_don
        WHERE ngay_tao >= :fromDate AND ngay_tao <= :toDate
        AND trang_thai = 'HOAN_THANH'
    """, nativeQuery = true)
    List<Map<String, Object>> getVoucherStats(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    // 6. LẤY DỮ LIỆU QUIZ DA (INSIGHT KHÁCH HÀNG) THẬT
    @Query(value = """
        SELECT ld.ten AS name, COUNT(kq.id) AS value
        FROM ket_qua_quiz kq
        JOIN loai_da ld ON kq.id_loai_da_ket_qua = ld.id
        WHERE kq.thoi_gian >= :fromDate AND kq.thoi_gian <= :toDate
        GROUP BY ld.ten
        ORDER BY value DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getQuizStats(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    // 7. LẤY SẢN PHẨM SẮP HẾT HÀNG (Tồn kho <= 15)
    @Query(value = """
        SELECT TOP 5 sp.ten AS name, ISNULL(SUM(ct.so_luong_ton), 0) AS value
        FROM chi_tiet_san_pham ct
        JOIN san_pham sp ON ct.id_san_pham = sp.id
        WHERE ct.trang_thai = 1
        GROUP BY sp.id, sp.ten
        HAVING SUM(ct.so_luong_ton) <= 15
        ORDER BY value ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getLowStockProducts();

    // 8. TỶ LỆ TRẠNG THÁI ĐƠN HÀNG ONLINE (Funnel)
    @Query(value = """
        SELECT trang_thai AS name, COUNT(id) AS value
        FROM hoa_don
        WHERE ngay_tao >= :fromDate AND ngay_tao <= :toDate
        AND loai_don = 'ONLINE'
        GROUP BY trang_thai
    """, nativeQuery = true)
    List<Map<String, Object>> getOrderStatusFunnel(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}