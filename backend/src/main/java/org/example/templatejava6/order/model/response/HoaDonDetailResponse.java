package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.HoaDon;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class HoaDonDetailResponse extends HoaDonResponse {

    private Integer idKhachHang;
    private Integer idNhanVien;
    private Integer idPhuongThucThanhToan;
    private Integer idPhieuGiamGia;
    private String soDienThoaiKhachHang;
    private BigDecimal soTienKhachDua;
    private BigDecimal tienThua;
    private String maGiaoDich;
    private List<HoaDonChiTietResponse> chiTiets;

    public HoaDonDetailResponse(HoaDon hd) {
        super(hd);
        this.idKhachHang = hd.getIdKhachHang() != null ? hd.getIdKhachHang().getId() : null;
        if (hd.getIdKhachHang() != null) {
            this.soDienThoaiKhachHang = hd.getIdKhachHang().getSoDienThoai();
        }
        this.idNhanVien = hd.getIdNhanVien() != null ? hd.getIdNhanVien().getId() : null;
        this.idPhuongThucThanhToan = hd.getIdPhuongThucThanhToan() != null
                ? hd.getIdPhuongThucThanhToan().getId()
                : null;
        this.idPhieuGiamGia = hd.getIdPhieuGiamGia() != null ? hd.getIdPhieuGiamGia().getId() : null;
    }
}
