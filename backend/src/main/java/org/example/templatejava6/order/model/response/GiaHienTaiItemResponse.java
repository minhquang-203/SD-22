package org.example.templatejava6.order.model.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GiaHienTaiItemResponse {

    private Integer idChiTietSanPham;
    private Integer idSanPham;
    private String tenSanPham;
    private BigDecimal giaBan;
    private BigDecimal giaGoc;
    private BigDecimal phanTramGiam;
    private Integer soLuongTon;
    private Boolean trangThaiBienThe;
    private Boolean trangThaiSanPham;
    private Boolean conBan;
}
