package org.example.templatejava6.review.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DanhGiaRequest {

    /** Null khi khách vãng lai đánh giá đơn không gắn tài khoản. */
    private Integer idKhachHang;

    @NotNull(message = "Id sản phẩm không được để trống")
    private Integer idSanPham;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao phải từ 1 đến 5")
    @Max(value = 5, message = "Số sao phải từ 1 đến 5")
    private Byte soSao;

    private String noiDung;

    private String hinhAnhVideo;

    @NotNull(message = "Chỉ đánh giá được sản phẩm trong đơn đã giao")
    private Integer idHoaDonChiTiet;

    /** Mã tra cứu đơn — bắt buộc với khách vãng lai (đơn không gắn tài khoản). */
    private String trackingToken;

    private String imageBase64;
}
