package org.example.templatejava6.order.repository;

import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YeuCauTraHangRepository extends JpaRepository<YeuCauTraHang, Integer> {

    List<YeuCauTraHang> findByIdHoaDonOrderByNgayTaoDesc(HoaDon hoaDon);

    List<YeuCauTraHang> findByIdHoaDon_IdKhachHang_IdOrderByNgayTaoDesc(Integer idKhachHang);

    List<YeuCauTraHang> findAllByOrderByNgayTaoDesc();

    List<YeuCauTraHang> findByTrangThaiOrderByNgayTaoDesc(TrangThaiTraHang trangThai);

    boolean existsByIdHoaDon_IdAndTrangThaiNotIn(Integer idHoaDon, java.util.Collection<TrangThaiTraHang> trangThaiKetThuc);
}
