package org.example.templatejava6.review.repository;

import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.review.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {

    List<DanhGia> findBySanPhamAndTrangThai(SanPham sanPham, String trangThai);

    List<DanhGia> findByTrangThai(String trangThai);

    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanPham.id = :idSanPham AND d.trangThai = :trangThai")
    Double findAverageRatingBySanPham(@Param("idSanPham") Integer idSanPham, @Param("trangThai") String trangThai);

    @Query("SELECT COUNT(d) FROM DanhGia d WHERE d.sanPham.id = :idSanPham AND d.trangThai = :trangThai")
    Long countApprovedBySanPham(@Param("idSanPham") Integer idSanPham, @Param("trangThai") String trangThai);

    default Double findAverageRatingBySanPham(Integer idSanPham) {
        return findAverageRatingBySanPham(idSanPham, "DA_DUYET");
    }

    default Long countApprovedBySanPham(Integer idSanPham) {
        return countApprovedBySanPham(idSanPham, "DA_DUYET");
    }
}
