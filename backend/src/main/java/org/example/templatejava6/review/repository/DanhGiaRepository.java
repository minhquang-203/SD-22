package org.example.templatejava6.review.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.review.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {

    List<DanhGia> findBySanPhamOrderByNgayTaoDesc(SanPham sanPham);

    Optional<DanhGia> findFirstByHoaDonChiTiet_Id(Integer idHoaDonChiTiet);

    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanPham.id = :idSanPham")
    Double findAverageRatingBySanPham(@Param("idSanPham") Integer idSanPham);

    @Query("SELECT COUNT(d) FROM DanhGia d WHERE d.sanPham.id = :idSanPham")
    Long countBySanPhamId(@Param("idSanPham") Integer idSanPham);
}
