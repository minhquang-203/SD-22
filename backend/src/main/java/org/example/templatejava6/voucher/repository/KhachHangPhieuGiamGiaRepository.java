package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.voucher.entity.KhachHangPhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhachHangPhieuGiamGiaRepository extends JpaRepository<KhachHangPhieuGiamGia, Integer> {

    boolean existsByKhachHang_IdAndPhieuGiamGia_Id(Integer idKhachHang, Integer idPhieuGiamGia);

    List<KhachHangPhieuGiamGia> findByPhieuGiamGia_Id(Integer idPhieuGiamGia);

    void deleteByPhieuGiamGia_IdAndKhachHang_Id(Integer idPhieuGiamGia, Integer idKhachHang);

    @Query("""
        SELECT k.phieuGiamGia FROM KhachHangPhieuGiamGia k
        WHERE k.khachHang.id = :idKhachHang
          AND k.phieuGiamGia.trangThai = true
        ORDER BY k.phieuGiamGia.ngayKetThuc ASC
        """)
    List<PhieuGiamGia> findVoucherByKhachHangId(@Param("idKhachHang") Integer idKhachHang);

    @Query("""
        SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END
        FROM KhachHangPhieuGiamGia k
        WHERE k.khachHang.id = :idKhachHang
          AND k.phieuGiamGia.id = :idPhieuGiamGia
        """)
    boolean daGanChoKhach(@Param("idKhachHang") Integer idKhachHang,
                          @Param("idPhieuGiamGia") Integer idPhieuGiamGia);
}
