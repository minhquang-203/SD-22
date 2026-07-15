package org.example.templatejava6.notification.repository;

import org.example.templatejava6.notification.entity.ThongBao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {

    /** Thông báo dành cho admin/hệ thống (không gắn khách cụ thể). */
    List<ThongBao> findByIdKhachHangIsNullOrderByThoiGianDescIdDesc(Pageable pageable);

    long countByIdKhachHangIsNullAndDaDocFalse();

    @Modifying
    @Query("update ThongBao t set t.daDoc = true where t.daDoc = false and t.idKhachHang is null")
    int markAllAdminRead();
}
