package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.AnhYeuCauTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnhYeuCauTraHangRepository extends JpaRepository<AnhYeuCauTraHang, Integer> {

    List<AnhYeuCauTraHang> findByIdYeuCauTraHang_IdOrderByIdAsc(Integer idYeuCauTraHang);
}
