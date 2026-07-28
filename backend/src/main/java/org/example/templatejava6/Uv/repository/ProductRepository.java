package org.example.templatejava6.Uv.repository;

import org.example.templatejava6.Uv.entity.SanPhamUV;
import org.example.templatejava6.product.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<SanPhamUV, Integer> {

    @Query(value = "SELECT sp.* FROM san_pham sp " +
            "JOIN san_pham_cong_dung spcd ON sp.id = spcd.id_san_pham " +
            "JOIN cong_dung cd ON spcd.id_cong_dung = cd.id " +
            "WHERE cd.ten = :tenCongDung", nativeQuery = true)
    List<SanPham> findByCongDungName(@Param("tenCongDung") String tenCongDung);

    // Mới: lọc sản phẩm theo từ khóa SPF (vd: "15", "30", "50")
    List<SanPhamUV> findByChiSoSpfContainingIgnoreCase(String spfKeyword);
}