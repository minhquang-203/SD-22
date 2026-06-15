package org.example.templatejava6.quiz.model.request;

import lombok.Data;
import java.util.List;

@Data
public class CauHoiQuizRequest {
    private String title; // Tương ứng với currentQuestion.title ở Vue
    private Integer thuTu;
    private List<DapAnRequest> answers; // Tương ứng với mảng currentQuestion.answers ở Vue

    @Data
    public static class DapAnRequest {
        private String icon;
        private String label;      // Noi dung dap an
        private Integer scoreValue; // Diem
        private Integer tagId;      // Chinh la id_loai_da
    }
}