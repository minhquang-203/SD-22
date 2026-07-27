package org.example.templatejava6.quiz.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineComboResponse {
    private Integer id;
    private String ten;
    private String moTa;
    private Integer idLoaiDa;
    private String tenLoaiDa;
    private Boolean trangThai;
    private Integer thuTu;
    private List<ChiTietResponse> chiTiets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietResponse {
        private Integer id;
        private Integer idSanPham;
        private String tenSanPham;
        private String anhChinhUrl;
        private String ghiChu;
        private Integer thuTu;
    }
}
