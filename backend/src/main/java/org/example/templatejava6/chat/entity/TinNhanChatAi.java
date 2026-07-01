package org.example.templatejava6.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.SanPham;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tin_nhan_chat_ai")
public class TinNhanChatAi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phien", nullable = false)
    private PhienChatAi phienChatAi;

    @Column(name = "nguoi_gui", nullable = false)
    private String nguoiGui; // KHACH hoặc AI

    @Column(name = "noi_dung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham_goi_y")
    private SanPham sanPhamGoiY;

    @Column(name = "thoi_gian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;
}
