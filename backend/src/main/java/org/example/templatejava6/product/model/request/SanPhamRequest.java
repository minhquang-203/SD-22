package org.example.templatejava6.product.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamRequest {

    private Integer id;

    private String maSanPham;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String ten;

    @NotNull(message = "Thương hiệu không được để trống")
    private Integer idThuongHieu;

    @NotNull(message = "Danh mục không được để trống")
    private Integer idDanhMuc;

    @NotNull(message = "Dạng sản phẩm không được để trống")
    private Integer idDangSanPham;

    private String chiSoSpf;
    private String chiSoPa;
    private String loaiChongNang;
    private Boolean khangNuoc;
    private Boolean noiBat;
    private String moTa;

    @Valid
    private List<ChiTietSanPhamRequest> chiTiets;

    @Valid
    private List<AnhSanPhamRequest> anhs;

    private List<Integer> idLoaiDas;
    private List<Integer> idCongDungs;
    private List<Integer> idThanhPhans;
}
