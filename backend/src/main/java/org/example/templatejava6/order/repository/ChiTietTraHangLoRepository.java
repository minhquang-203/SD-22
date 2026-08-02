package org.example.templatejava6.order.repository;

import org.example.templatejava6.order.entity.ChiTietTraHangLo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietTraHangLoRepository extends JpaRepository<ChiTietTraHangLo, Integer> {

    List<ChiTietTraHangLo> findByYeuCauTraHang_IdOrderByIdAsc(Integer idYeuCauTraHang);
}
