package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.voucher.entity.ChiTietDotGiamGia;
import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {

    @Query("""
            SELECT sp.id AS sanPhamId,
                   MIN(ctsp.giaBan) AS giaGocMin,
                   MAX(ctsp.giaBan) AS giaGocMax,
                   MIN(ct.giaSauGiam) AS giaSauGiamMin,
                   MAX(ct.giaSauGiam) AS giaSauGiamMax,
                   MAX(dgg.phanTramGiam) AS phanTramGiam
            FROM ChiTietDotGiamGia ct
            JOIN ct.idDotGiamGia dgg
            JOIN ct.idChiTietSanPham ctsp
            JOIN ctsp.sanPham sp
            WHERE dgg.trangThai = true
              AND dgg.isActive = true
              AND dgg.ngayBatDau IS NOT NULL
              AND dgg.ngayKetThuc IS NOT NULL
              AND dgg.ngayBatDau <= CURRENT_TIMESTAMP
              AND dgg.ngayKetThuc >= CURRENT_TIMESTAMP
              AND sp.trangThai = true
              AND (ctsp.trangThai IS NULL OR ctsp.trangThai = true)
            GROUP BY sp.id
            """)
    List<SalePriceAgg> aggregateActiveSalePrices();

    @Query("""
            SELECT ct.idChiTietSanPham.id AS chiTietSanPhamId,
                   ct.idChiTietSanPham.giaBan AS giaGoc,
                   ct.giaSauGiam AS giaSauGiam,
                   dgg.phanTramGiam AS phanTramGiam
            FROM ChiTietDotGiamGia ct
            JOIN ct.idDotGiamGia dgg
            JOIN ct.idChiTietSanPham ctsp
            WHERE dgg.trangThai = true
              AND dgg.isActive = true
              AND dgg.ngayBatDau IS NOT NULL
              AND dgg.ngayKetThuc IS NOT NULL
              AND dgg.ngayBatDau <= CURRENT_TIMESTAMP
              AND dgg.ngayKetThuc >= CURRENT_TIMESTAMP
              AND (ctsp.trangThai IS NULL OR ctsp.trangThai = true)
            """)
    List<VariantSalePriceRow> findActiveVariantSaleRows();

    @Query("""
            SELECT DISTINCT ct.idChiTietSanPham.sanPham.id
            FROM ChiTietDotGiamGia ct
            JOIN ct.idDotGiamGia dgg
            JOIN ct.idChiTietSanPham ctsp
            JOIN ctsp.sanPham sp
            WHERE dgg.trangThai = true
              AND dgg.isActive = true
              AND dgg.ngayBatDau IS NOT NULL
              AND dgg.ngayKetThuc IS NOT NULL
              AND dgg.ngayBatDau <= CURRENT_TIMESTAMP
              AND dgg.ngayKetThuc >= CURRENT_TIMESTAMP
              AND sp.trangThai = true
            """)
    List<Integer> findSanPhamIdsInActiveSales();

    List<ChiTietDotGiamGia> findByIdDotGiamGia(DotGiamGia dotGiamGia);

    boolean existsByIdDotGiamGiaAndIdChiTietSanPham_Id(DotGiamGia dotGiamGia, Integer idChiTietSanPham);

    boolean existsByIdDotGiamGiaAndIdChiTietSanPham_IdAndIdNot(DotGiamGia dotGiamGia, Integer idChiTietSanPham, Integer id);
}
