package org.example.templatejava6.quiz.model.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KetQuaQuizToiResponse {
    private Integer id;
    private Integer idLoaiDa;
    private String tenLoaiDa;
    private String moTaLoaiDa;
    private LocalDateTime thoiGianLam;
}
