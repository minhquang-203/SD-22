package org.example.templatejava6.nhaphang.repository;

import org.example.templatejava6.nhaphang.entity.NhaCungCap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Integer> {

    List<NhaCungCap> findByTrangThaiTrueOrderByTenAsc();

    @Query("""
            SELECT n FROM NhaCungCap n
            WHERE n.trangThai = true
              AND (:q IS NULL OR :q = ''
                   OR LOWER(n.ma) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(n.ten) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY n.ten ASC
            """)
    List<NhaCungCap> searchActive(@Param("q") String q);

    @Query("""
            SELECT n FROM NhaCungCap n
            WHERE (:q IS NULL OR :q = ''
                   OR LOWER(n.ma) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(n.ten) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY n.trangThai DESC, n.ten ASC
            """)
    List<NhaCungCap> searchAll(@Param("q") String q);
}
