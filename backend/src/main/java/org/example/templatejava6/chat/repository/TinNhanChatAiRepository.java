package org.example.templatejava6.chat.repository;

import org.example.templatejava6.chat.entity.TinNhanChatAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanChatAiRepository extends JpaRepository<TinNhanChatAi, Integer> {
    List<TinNhanChatAi> findByPhienChatAiIdOrderByThoiGianAsc(Integer idPhien);
}
