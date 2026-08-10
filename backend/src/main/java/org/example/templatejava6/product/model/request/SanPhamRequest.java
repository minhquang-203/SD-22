package org.example.templatejava6.product.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 200, message = "Tên sản phẩm phải từ 2 đến 200 ký tự")
    private String ten;

    @NotNull(message = "Thương hiệu không được để trống")
    private Integer idThuongHieu;

    @NotNull(message = "Danh mục không được để trống")
    private Integer idDanhMuc;

    @NotNull(message = "Dạng sản phẩm không được để trống")
    private Integer idDangSanPham;

    @NotBlank(message = "Chỉ số SPF không được để trống")
    @Size(max = 30, message = "Chỉ số SPF quá dài")
    private String chiSoSpf;

    @NotBlank(message = "Chỉ số PA không được để trống")
    @Size(max = 20, message = "Chỉ số PA quá dài")
    private String chiSoPa;

    @NotBlank(message = "Loại chống nắng không được để trống")
    private String loaiChongNang;

    private Boolean khangNuoc;
    private Boolean noiBat;
    private String moTa;

    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 biến thể")
    @Valid
    private List<ChiTietSanPhamRequest> chiTiets;

    @Valid
    private List<AnhSanPhamRequest> anhs;

    private List<Integer> idLoaiDas;
    private List<Integer> idCongDungs;
    private List<Integer> idThanhPhans;
}
