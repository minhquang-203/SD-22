package org.example.templatejava6.support.repository;

import org.example.templatejava6.support.entity.TinNhanHoTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TinNhanHoTroRepository extends JpaRepository<TinNhanHoTro, Integer> {

    List<TinNhanHoTro> findByIdPhien_IdOrderByThoiGianAsc(Integer idPhien);

    Optional<TinNhanHoTro> findFirstByIdPhien_IdOrderByThoiGianDesc(Integer idPhien);

    long countByIdPhien_IdAndNguoiGuiAndDaDocFalse(Integer idPhien, String nguoiGui);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TinNhanHoTro t
            SET t.daDoc = true
            WHERE t.idPhien.id = :idPhien
              AND t.nguoiGui = :nguoiGui
              AND t.daDoc = false
            """)
    int markDaDocByPhienAndNguoiGui(
            @Param("idPhien") Integer idPhien,
            @Param("nguoiGui") String nguoiGui);

    @Query("""
            SELECT t FROM TinNhanHoTro t
            JOIN FETCH t.idPhien p
            WHERE t.id = :id
            """)
    Optional<TinNhanHoTro> findDetailById(Integer id);
}
