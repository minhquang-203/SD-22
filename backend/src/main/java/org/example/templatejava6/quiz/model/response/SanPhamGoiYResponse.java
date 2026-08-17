package org.example.templatejava6.quiz.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamGoiYResponse {
    private Integer id;
    private String ten;
    private String anhChinhUrl;
    private BigDecimal giaBan;
}
