package org.example.templatejava6.customer.repository;

import org.example.templatejava6.common.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    @Query("SELECT k FROM KhachHang k WHERE LOWER(k.email) = LOWER(:taiKhoan) OR k.soDienThoai = :taiKhoan")
    Optional<KhachHang> findByEmailOrSoDienThoai(@Param("taiKhoan") String taiKhoan);

    boolean existsByMaKhachHang(String maKhachHang);

    @Query("SELECT k FROM KhachHang k WHERE "
            + "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(k.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "k.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    List<KhachHang> timKiem(@Param("keyword") String keyword);

    @Query("SELECT k FROM KhachHang k WHERE "
            + "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(k.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "k.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    Page<KhachHang> timKiem(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Lọc khách hàng theo các tiêu chí nhóm (tất cả optional, kết hợp bằng AND)
     * để gán voucher hàng loạt. Chỉ lấy khách đang hoạt động và có vai trò khách.
     */
    @Query("""
        SELECT k FROM KhachHang k
        WHERE k.trangThai = true
          AND (:diemToiThieu IS NULL OR k.diemTichLuy >= :diemToiThieu)
          AND (:tuNgayTao IS NULL OR k.ngayTao >= :tuNgayTao)
          AND (:thangSinhNhat IS NULL OR (k.ngaySinh IS NOT NULL AND FUNCTION('MONTH', k.ngaySinh) = :thangSinhNhat))
          AND (:gioiTinh IS NULL OR :gioiTinh = '' OR k.gioiTinh = :gioiTinh)
          AND (:idLoaiDa IS NULL OR k.loaiDa.id = :idLoaiDa)
        """)
    List<KhachHang> locTheoNhom(
            @Param("diemToiThieu") Integer diemToiThieu,
            @Param("tuNgayTao") LocalDateTime tuNgayTao,
            @Param("thangSinhNhat") Integer thangSinhNhat,
            @Param("gioiTinh") String gioiTinh,
            @Param("idLoaiDa") Integer idLoaiDa);
}
