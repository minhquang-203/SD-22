package org.example.templatejava6.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "anh_yeu_cau_tra_hang")
public class AnhYeuCauTraHang {

    public static final String LOAI_KHACH = "KHACH";
    public static final String LOAI_TU_CHOI = "TU_CHOI";

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
    @Column(name = "duong_dan", nullable = false, length = 500)
    private String duongDan;

    @Size(max = 20)
    @ColumnDefault("'KHACH'")
    @Column(name = "loai", length = 20)
    private String loai;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    public boolean laAnhTuChoi() {
        return LOAI_TU_CHOI.equalsIgnoreCase(loai);
    }
}
