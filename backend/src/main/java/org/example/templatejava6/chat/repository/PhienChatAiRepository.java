package org.example.templatejava6.chat.repository;

import org.example.templatejava6.chat.entity.PhienChatAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhienChatAiRepository extends JpaRepository<PhienChatAi, Integer> {
    List<PhienChatAi> findByKhachHangIdOrderByThoiGianBatDauDesc(Integer idKhachHang);
}
