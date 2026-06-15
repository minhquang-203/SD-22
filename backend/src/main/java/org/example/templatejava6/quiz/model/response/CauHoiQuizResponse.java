package org.example.templatejava6.quiz.model.response;

import lombok.Data;
import java.util.List;

@Data
public class CauHoiQuizResponse {
    private Integer id;
    private String title;
    private List<DapAnResponse> answers;

    @Data
    public static class DapAnResponse {
        private String icon;
        private String label;
        private Integer scoreValue;
        private Integer tagId;
    }
}