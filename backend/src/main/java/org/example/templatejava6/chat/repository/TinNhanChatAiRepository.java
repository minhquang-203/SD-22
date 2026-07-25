package org.example.templatejava6.chat.repository;

import org.example.templatejava6.chat.entity.TinNhanChatAi;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanChatAiRepository extends JpaRepository<TinNhanChatAi, Integer> {
    List<TinNhanChatAi> findByPhienChatAiIdOrderByThoiGianAsc(Integer idPhien);

    @Query("SELECT t FROM TinNhanChatAi t WHERE t.phienChatAi.id = :idPhien ORDER BY t.thoiGian DESC, t.id DESC")
    List<TinNhanChatAi> findRecentByPhien(@Param("idPhien") Integer idPhien, Pageable pageable);
}
