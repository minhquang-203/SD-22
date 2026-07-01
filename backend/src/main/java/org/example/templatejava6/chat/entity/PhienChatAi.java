package org.example.templatejava6.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.KhachHang;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "phien_chat_ai")
public class PhienChatAi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "thoi_gian_bat_dau", insertable = false, updatable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "trang_thai")
    private String trangThai;
}
