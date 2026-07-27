package org.example.templatejava6.quiz.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineComboRequest {
    private String ten;
    private String moTa;
    private Integer idLoaiDa;
    private Boolean trangThai;
    private List<ChiTietRequest> chiTiets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietRequest {
        private Integer idSanPham;
        private Integer thuTu;
        private String ghiChu;
    }
}
