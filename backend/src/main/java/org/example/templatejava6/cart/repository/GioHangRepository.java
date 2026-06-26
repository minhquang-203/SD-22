package org.example.templatejava6.cart.repository;

import org.example.templatejava6.cart.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    Optional<GioHang> findFirstByKhachHang_IdOrderByIdAsc(Integer idKhachHang);
}
