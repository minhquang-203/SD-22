package org.example.templatejava6.order.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LichSuDonHangCapNhatRequest {

    private String ghiChu;

    private Integer idNhanVien;
}
