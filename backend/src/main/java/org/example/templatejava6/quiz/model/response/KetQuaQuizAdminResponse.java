package org.example.templatejava6.quiz.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaQuizAdminResponse {
    private Integer id;
    private String tenKhachHang;
    private String emailKhachHang;
    private String sdtKhachHang;
    private Integer idLoaiDa;
    private String maLoaiDa;
    private String tenLoaiDa;
    private String moTaLoaiDa;
    private LocalDateTime thoiGian;
}
