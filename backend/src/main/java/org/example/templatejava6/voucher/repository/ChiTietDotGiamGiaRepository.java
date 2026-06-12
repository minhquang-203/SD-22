package org.example.templatejava6.voucher.repository;

import org.example.templatejava6.voucher.entity.ChiTietDotGiamGia;
import org.example.templatejava6.voucher.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {

    List<ChiTietDotGiamGia> findByIdDotGiamGia(DotGiamGia dotGiamGia);

    boolean existsByIdDotGiamGiaAndIdChiTietSanPham_Id(DotGiamGia dotGiamGia, Integer idChiTietSanPham);

    boolean existsByIdDotGiamGiaAndIdChiTietSanPham_IdAndIdNot(DotGiamGia dotGiamGia, Integer idChiTietSanPham, Integer id);
}
