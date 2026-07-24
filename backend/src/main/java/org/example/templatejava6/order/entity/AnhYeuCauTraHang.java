package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "anh_yeu_cau_tra_hang")
public class AnhYeuCauTraHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_yeu_cau_tra_hang", nullable = false)
    private YeuCauTraHang idYeuCauTraHang;

    @NotNull
    @Size(max = 500)
    @Nationalized
    @Column(name = "duong_dan", nullable = false, length = 500)
    private String duongDan;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
