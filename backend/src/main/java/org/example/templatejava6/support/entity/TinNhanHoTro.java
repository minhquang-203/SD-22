package org.example.templatejava6.support.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tin_nhan_ho_tro")
public class TinNhanHoTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phien", nullable = false)
    private PhienHoTro idPhien;

    @Column(name = "nguoi_gui", nullable = false, length = 15)
    private String nguoiGui;

    @Column(name = "id_nguoi_gui")
    private Integer idNguoiGui;

    @Nationalized
    @Column(name = "noi_dung", nullable = false, length = 2000)
    private String noiDung;

    @Column(name = "da_doc", nullable = false)
    @ColumnDefault("0")
    private Boolean daDoc = false;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;
}
