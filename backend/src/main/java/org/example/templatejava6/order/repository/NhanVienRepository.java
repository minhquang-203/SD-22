package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    List<NhanVien> findByTrangThaiTrue();

    @Query("""
            SELECT n FROM NhanVien n JOIN FETCH n.vaiTro v
            WHERE n.trangThai = true
            ORDER BY
              CASE v.maVaiTro
                WHEN 'CHU' THEN 1
                WHEN 'QUAN_LY' THEN 2
                WHEN 'NHAN_VIEN' THEN 3
                ELSE 4
              END,
              n.id DESC
            """)
    List<NhanVien> findActiveWithVaiTro();

    @Query("""
            SELECT n FROM NhanVien n LEFT JOIN FETCH n.vaiTro v
            ORDER BY
              CASE v.maVaiTro
                WHEN 'CHU' THEN 1
                WHEN 'QUAN_LY' THEN 2
                WHEN 'NHAN_VIEN' THEN 3
                ELSE 4
              END,
              CASE WHEN n.trangThai = false THEN 1 ELSE 0 END,
              n.id DESC
            """)
    List<NhanVien> findAllWithVaiTro();

    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.vaiTro WHERE n.id = :id")
    Optional<NhanVien> findByIdWithVaiTro(@Param("id") Integer id);

    @Query("SELECT n.maNhanVien FROM NhanVien n WHERE n.maNhanVien IS NOT NULL")
    List<String> findAllMaNhanVien();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    @Query("SELECT n FROM NhanVien n WHERE LOWER(n.email) = LOWER(:taiKhoan) OR n.soDienThoai = :taiKhoan")
    Optional<NhanVien> findByEmailOrSoDienThoai(@Param("taiKhoan") String taiKhoan);
}
