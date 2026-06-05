package org.example.templatejava6.product.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.SanPham;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SanPhamDetailResponse extends SanPhamResponse {

    private String moTa;
    private Integer idThuongHieu;
    private Integer idDanhMuc;
    private Integer idDangSanPham;
    private List<ChiTietSanPhamResponse> chiTiets;
    private List<AnhSanPhamResponse> anhs;
    private List<Integer> idLoaiDas;
    private List<Integer> idCongDungs;
    private List<Integer> idThanhPhans;
    private Double diemTrungBinh;
    private Long soLuongDanhGia;

    public SanPhamDetailResponse(SanPham sp) {
        super(sp);
        this.moTa = sp.getMoTa();
        this.idThuongHieu = sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null;
        this.idDanhMuc = sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null;
        this.idDangSanPham = sp.getDangSanPham() != null ? sp.getDangSanPham().getId() : null;
    }
}
