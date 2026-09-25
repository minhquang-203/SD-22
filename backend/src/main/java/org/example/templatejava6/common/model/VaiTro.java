package org.example.templatejava6.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vai_tro")
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @Column(name = "ma_vai_tro", length = 20)
    private String maVaiTro;

    @Size(max = 50)
    @Column(name = "ten_vai_tro", length = 50)
    private String tenVaiTro;
    @Column(name = "mo_ta", length = 255)
    private String moTa;
}
