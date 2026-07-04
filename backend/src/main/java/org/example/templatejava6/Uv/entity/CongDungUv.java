package org.example.templatejava6.Uv.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cong_dung")
@Data
public class CongDungUv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten")
    private String ten;
}
