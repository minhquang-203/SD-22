package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.YeuCauTraHang;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StorefrontReturnDetailResponse extends YeuCauTraHangResponse {

    private List<StorefrontOrderLineResponse> chiTiets = new ArrayList<>();
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal soTienHoan;
    private String trangThaiHoanTien;
    private String trangThaiHoanTienLabel;
    private String maGiaoDichHoan;
    private String phuongThucHoan;
    private LocalDateTime ngayHoan;
    private String diaChiGiao;
    private List<StorefrontReturnTimelineStepResponse> timeline = new ArrayList<>();

    public StorefrontReturnDetailResponse(YeuCauTraHang yc, List<String> anhUrls) {
        super(yc, anhUrls);
    }
}
