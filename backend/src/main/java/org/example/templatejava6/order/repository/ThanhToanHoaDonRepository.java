package org.example.templatejava6.order.repository;

import jakarta.persistence.LockModeType;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThanhToanHoaDonRepository extends JpaRepository<ThanhToanHoaDon, Integer> {

    List<ThanhToanHoaDon> findByIdHoaDonOrderByThoiGianDesc(HoaDon hoaDon);

    List<ThanhToanHoaDon> findByIdHoaDonAndTrangThaiOrderByThoiGianDesc(HoaDon hoaDon, String trangThai);

    Optional<ThanhToanHoaDon> findByMaGiaoDich(String maGiaoDich);

    /**
     * Khóa bản ghi giao dịch khi xử lý kết quả thanh toán (callback/IPN) để tránh
     * hai luồng cùng chuyển trạng thái và chạy side-effect (mail, thông báo, trừ giỏ) hai lần.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ThanhToanHoaDon t where t.maGiaoDich = :maGiaoDich")
    Optional<ThanhToanHoaDon> findByMaGiaoDichForUpdate(@Param("maGiaoDich") String maGiaoDich);

    void deleteByIdHoaDon(HoaDon hoaDon);

    default Optional<ThanhToanHoaDon> findLatestByHoaDon(HoaDon hoaDon) {
        List<ThanhToanHoaDon> list = findByIdHoaDonOrderByThoiGianDesc(hoaDon);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    default Optional<ThanhToanHoaDon> findLatestByHoaDonAndTrangThai(HoaDon hoaDon, String trangThai) {
        List<ThanhToanHoaDon> list = findByIdHoaDonAndTrangThaiOrderByThoiGianDesc(hoaDon, trangThai);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
