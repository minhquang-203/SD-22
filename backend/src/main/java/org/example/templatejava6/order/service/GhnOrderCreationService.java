package org.example.templatejava6.order.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.shipping.config.GhnProperties;
import org.example.templatejava6.shipping.model.request.CreateShippingOrderRequest;
import org.example.templatejava6.shipping.model.response.CreateShippingOrderResponse;
import org.example.templatejava6.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tao van don Giao Hang Nhanh (GHN) cho mot hoa don va luu lai ma van don.
 *
 * <p>Idempotent: neu hoa don da co {@code maVanDonGhn} thi bo qua. Yeu cau hoa don da luu
 * {@code ghnDistrictId} va {@code ghnWardCode} (lay tu buoc chon dia chi khi dat hang).</p>
 */
@Service
public class GhnOrderCreationService {

    private static final Logger log = LoggerFactory.getLogger(GhnOrderCreationService.class);
    private static final String MA_COD = "COD";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final ShippingService shippingService;
    private final GhnProperties ghnProperties;

    public GhnOrderCreationService(HoaDonRepository hoaDonRepository,
                                   HoaDonChiTietRepository hoaDonChiTietRepository,
                                   LichSuDonHangRepository lichSuDonHangRepository,
                                   ShippingService shippingService,
                                   GhnProperties ghnProperties) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.shippingService = shippingService;
        this.ghnProperties = ghnProperties;
    }

    /**
     * Tao van don neu can (best-effort): khong nem loi de khong lam gian doan luong dat hang
     * hay chuyen trang thai. Bo qua khi da co van don / chua cau hinh GHN / thieu dia chi GHN.
     */
    @Transactional
    public KetQua taoVanDonNeuCan(HoaDon hoaDon) {
        if (hoaDon == null) {
            return KetQua.boQua("Khong co hoa don.");
        }
        if (daCoVanDon(hoaDon)) {
            return KetQua.boQua("Don da co ma van don GHN.");
        }
        if (!ghnProperties.isFeeConfigured()) {
            return KetQua.boQua("Chua cau hinh GHN (ShopId / kho gui).");
        }
        if (hoaDon.getGhnDistrictId() == null
                || hoaDon.getGhnWardCode() == null || hoaDon.getGhnWardCode().isBlank()) {
            return KetQua.boQua("Don thieu ma quan/huyen hoac phuong/xa GHN.");
        }
        try {
            return taoVanDon(hoaDon);
        } catch (RuntimeException ex) {
            log.warn("Tao van don GHN that bai cho don {}: {}", hoaDon.getMaHoaDon(), ex.getMessage());
            ghiLichSu(hoaDon, "Tao van don GHN that bai: " + ex.getMessage());
            return KetQua.loi("Tao van don GHN that bai: " + ex.getMessage());
        }
    }

    /**
     * Tao van don theo id (dung cho endpoint thu cong). Nem ApiException de bao loi ro rang cho admin.
     */
    @Transactional
    public KetQua taoVanDonTheoId(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("Khong tim thay hoa don.", "NOT_FOUND"));
        if (daCoVanDon(hoaDon)) {
            return KetQua.boQua("Don da co ma van don GHN: " + hoaDon.getMaVanDonGhn());
        }
        if (!ghnProperties.isFeeConfigured()) {
            throw new ApiException("Chua cau hinh ShopId / kho gui cua GHN.", "GHN_NOT_CONFIGURED");
        }
        if (hoaDon.getGhnDistrictId() == null
                || hoaDon.getGhnWardCode() == null || hoaDon.getGhnWardCode().isBlank()) {
            throw new ApiException(
                    "Don chua co ma quan/huyen hoac phuong/xa GHN nguoi nhan, khong the tao van don.",
                    "GHN_MISSING_ADDRESS");
        }
        return taoVanDon(hoaDon);
    }

    private KetQua taoVanDon(HoaDon hoaDon) {
        CreateShippingOrderRequest request = buildRequest(hoaDon);
        CreateShippingOrderResponse response = shippingService.createOrder(request);

        hoaDon.setMaVanDonGhn(response.getOrderCode());
        hoaDonRepository.save(hoaDon);

        String ghiChu = "Da tao van don GHN: " + response.getOrderCode();
        if (response.getExpectedDeliveryTime() != null && !response.getExpectedDeliveryTime().isBlank()) {
            ghiChu += " (du kien giao: " + response.getExpectedDeliveryTime() + ")";
        }
        ghiLichSu(hoaDon, ghiChu);

        log.info("Da tao van don GHN {} cho don {}", response.getOrderCode(), hoaDon.getMaHoaDon());
        return KetQua.thanhCong(response.getOrderCode());
    }

    private CreateShippingOrderRequest buildRequest(HoaDon hoaDon) {
        CreateShippingOrderRequest request = new CreateShippingOrderRequest();
        request.setToName(orElse(hoaDon.getTenNguoiNhan(),
                hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getHoTen() : "Khach hang"));
        request.setToPhone(orElse(hoaDon.getSdtNguoiNhan(),
                hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getSoDienThoai() : null));
        request.setToAddress(orElse(hoaDon.getDiaChiGiao(), "Dia chi nhan hang"));
        request.setToDistrictId(hoaDon.getGhnDistrictId());
        request.setToWardCode(hoaDon.getGhnWardCode());
        request.setCodAmount(tinhCodAmount(hoaDon));
        request.setItems(buildItems(hoaDon));
        return request;
    }

    /**
     * Voi don COD, thu ho tien hang (sau giam gia). Khong gom phi van chuyen vi GHN dang dat
     * {@code payment_type_id = 2} (nguoi nhan tra phi ship truc tiep cho GHN), tranh thu trung phi.
     * Voi don da thanh toan (VNPAY), khong thu ho.
     */
    private Long tinhCodAmount(HoaDon hoaDon) {
        boolean laCod = hoaDon.getIdPhuongThucThanhToan() != null
                && MA_COD.equalsIgnoreCase(hoaDon.getIdPhuongThucThanhToan().getMa());
        if (!laCod || hoaDon.getThanhTien() == null) {
            return null;
        }
        BigDecimal phiVanChuyen = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO;
        BigDecimal tienThuHo = hoaDon.getThanhTien().subtract(phiVanChuyen).max(BigDecimal.ZERO);
        return tienThuHo.longValue();
    }

    private List<CreateShippingOrderRequest.Item> buildItems(HoaDon hoaDon) {
        List<CreateShippingOrderRequest.Item> items = new ArrayList<>();
        for (HoaDonChiTiet ct : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
            CreateShippingOrderRequest.Item item = new CreateShippingOrderRequest.Item();
            item.setName(tenSanPham(ct));
            item.setQuantity(ct.getSoLuong() != null && ct.getSoLuong() > 0 ? ct.getSoLuong() : 1);
            items.add(item);
        }
        return items;
    }

    private static String tenSanPham(HoaDonChiTiet ct) {
        if (ct.getIdChiTietSanPham() != null) {
            SanPham sp = ct.getIdChiTietSanPham().getSanPham();
            if (sp != null && sp.getTen() != null && !sp.getTen().isBlank()) {
                return sp.getTen();
            }
        }
        return "San pham";
    }

    private boolean daCoVanDon(HoaDon hoaDon) {
        return hoaDon.getMaVanDonGhn() != null && !hoaDon.getMaVanDonGhn().isBlank();
    }

    private void ghiLichSu(HoaDon hoaDon, String ghiChu) {
        LichSuDonHang ls = new LichSuDonHang();
        ls.setIdHoaDon(hoaDon);
        ls.setTrangThai(hoaDon.getTrangThai() != null ? hoaDon.getTrangThai().name() : "GHN");
        ls.setGhiChu(ghiChu != null && ghiChu.length() > 255 ? ghiChu.substring(0, 255) : ghiChu);
        ls.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(ls);
    }

    private static String orElse(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    /**
     * Ket qua tao van don, dung cho endpoint thu cong va logging.
     */
    public record KetQua(boolean thanhCong, String maVanDon, String thongDiep) {
        static KetQua thanhCong(String maVanDon) {
            return new KetQua(true, maVanDon, "Da tao van don GHN: " + maVanDon);
        }

        static KetQua boQua(String thongDiep) {
            return new KetQua(false, null, thongDiep);
        }

        static KetQua loi(String thongDiep) {
            return new KetQua(false, null, thongDiep);
        }
    }
}
