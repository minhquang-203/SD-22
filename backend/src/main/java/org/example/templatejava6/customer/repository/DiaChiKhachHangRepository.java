package org.example.templatejava6.customer.repository;

import org.example.templatejava6.customer.entity.DiaChiKhachHang;
import org.example.templatejava6.common.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Integer> {

    List<DiaChiKhachHang> findByKhachHang(KhachHang khachHang);
}
