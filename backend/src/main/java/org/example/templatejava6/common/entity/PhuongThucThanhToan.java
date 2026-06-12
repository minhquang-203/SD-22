package org.example.templatejava6.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Column(name = "ma", nullable = false, length = 20)
    private String ma;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 50)
    private String ten;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

}