package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.YeuCauMuaSoLuongLon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface YeuCauMuaSoLuongLonRepository extends JpaRepository<YeuCauMuaSoLuongLon, Integer> {

    @Query("""
            SELECT CASE WHEN COUNT(y) > 0 THEN true ELSE false END
            FROM YeuCauMuaSoLuongLon y
            WHERE y.soDienThoai = :sdt
              AND y.idChiTietSanPham.id = :idCtsp
              AND y.ngayTao >= :since
            """)
    boolean existsRecentDuplicate(
            @Param("sdt") String sdt,
            @Param("idCtsp") Integer idCtsp,
            @Param("since") LocalDateTime since);

    @Query("""
            SELECT y FROM YeuCauMuaSoLuongLon y
            LEFT JOIN FETCH y.idChiTietSanPham ct
            LEFT JOIN FETCH ct.sanPham
            LEFT JOIN FETCH ct.mauSac
            LEFT JOIN FETCH y.idKhachHang
            LEFT JOIN FETCH y.idNhanVienXuLy
            WHERE y.id = :id
            """)
    Optional<YeuCauMuaSoLuongLon> findDetailById(@Param("id") Integer id);

    @Query("""
            SELECT y FROM YeuCauMuaSoLuongLon y
            WHERE (:trangThai IS NULL OR :trangThai = '' OR y.trangThai = :trangThai)
            """)
    Page<YeuCauMuaSoLuongLon> search(@Param("trangThai") String trangThai, Pageable pageable);

    long countByTrangThai(String trangThai);
}
