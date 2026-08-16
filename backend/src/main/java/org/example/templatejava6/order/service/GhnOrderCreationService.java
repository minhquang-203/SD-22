package org.example.templatejava6.order.service;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
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
 * <p>Idempotent: neu hoa don da co {@code maVanDonGhn} thi bo qua. Dia chi 2 cap dung
 * {@code ghnProvinceName} + {@code ghnWardName} (ten tinh/phuong luc dat hang).</p>
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
        if (!duDiaChiGhn(hoaDon)) {
            return KetQua.boQua("Don thieu ten tinh/phuong hoac ma quan/huyen GHN.");
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
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn.", "NOT_FOUND"));
        if (!canTaoVanDonGhn(hoaDon.getTrangThai())) {
            throw new ApiException(
                    "Cần xác nhận đơn hàng trước khi tạo vận đơn Giao Hàng Nhanh.",
                    "INVALID_ORDER_STATUS");
        }
        if (daCoVanDon(hoaDon)) {
            return KetQua.boQua("Đơn đã có mã vận đơn GHN: " + hoaDon.getMaVanDonGhn());
        }
        if (!ghnProperties.isFeeConfigured()) {
            throw new ApiException("Chưa cấu hình ShopId / kho gửi của GHN.", "GHN_NOT_CONFIGURED");
        }
        if (!duDiaChiGhn(hoaDon)) {
            throw new ApiException(
                    "Đơn thiếu tên tỉnh/thành hoặc phường/xã người nhận, không thể tạo vận đơn GHN.",
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
        // GHN geocode theo to_address, nên bỏ tiền tố "Họ tên - SĐT, " của diaChiGiao.
        request.setToAddress(orElse(boTienToNguoiNhan(hoaDon.getDiaChiGiao()), "Dia chi nhan hang"));
        request.setToWardCode(hoaDon.getGhnWardCode());
        boolean newAddress = ShippingService.looksLikeNewWardCode(hoaDon.getGhnWardCode())
                || (coGiaTri(hoaDon.getGhnProvinceName()) && coGiaTri(hoaDon.getGhnWardName()));
        request.setIsNewToAddress(newAddress);
        if (newAddress) {
            TenDiaChi ten = resolveTenDiaChi(hoaDon);
            if (!coGiaTri(ten.provinceName()) || !coGiaTri(ten.wardName())) {
                throw new ApiException(
                        "Đơn thiếu tên tỉnh/thành hoặc phường/xã người nhận nên không tạo được "
                                + "vận đơn GHN (địa chỉ 2 cấp). Cập nhật lại địa chỉ giao của đơn.",
                        "GHN_MISSING_ADDRESS");
            }
            request.setToProvinceName(ten.provinceName());
            request.setToWardName(ten.wardName());
        } else {
            request.setToDistrictId(hoaDon.getGhnDistrictId());
        }
        request.setCodAmount(tinhCodAmount(hoaDon));
        request.setItems(buildItems(hoaDon));
        return request;
    }

    /**
     * Ten tinh/phuong de gui GHN. Uu tien cot da luu luc dat hang; cac don tao truoc khi co
     * cot nay thi tam suy ra tu diaChiGiao (chi dung lam fallback vi de sai khi dia chi cu the
     * co dau phay hoac chuoi bi cat 255 ky tu).
     */
    private static TenDiaChi resolveTenDiaChi(HoaDon hoaDon) {
        String provinceName = hoaDon.getGhnProvinceName();
        String wardName = hoaDon.getGhnWardName();
        if (coGiaTri(provinceName) && coGiaTri(wardName)) {
            return new TenDiaChi(provinceName.trim(), wardName.trim());
        }
        // diaChiGiao thường dạng: "Họ tên - SĐT, địa chỉ cụ thể, Phường/Xã, Tỉnh"
        String[] parts = splitAddressParts(hoaDon.getDiaChiGiao());
        if (!coGiaTri(provinceName) && parts.length >= 1) {
            provinceName = parts[parts.length - 1];
        }
        if (!coGiaTri(wardName) && parts.length >= 2) {
            wardName = parts[parts.length - 2];
        }
        log.warn("Don {} thieu ten tinh/phuong GHN, suy ra tu diaChiGiao: tinh={}, phuong={}",
                hoaDon.getMaHoaDon(), provinceName, wardName);
        return new TenDiaChi(provinceName, wardName);
    }

    private record TenDiaChi(String provinceName, String wardName) {
    }

    private static String boTienToNguoiNhan(String diaChiGiao) {
        if (!coGiaTri(diaChiGiao)) {
            return null;
        }
        int firstComma = diaChiGiao.indexOf(',');
        if (firstComma >= 0 && diaChiGiao.substring(0, firstComma).contains(" - ")) {
            return diaChiGiao.substring(firstComma + 1).trim();
        }
        return diaChiGiao.trim();
    }

    private static String[] splitAddressParts(String diaChiGiao) {
        String rest = boTienToNguoiNhan(diaChiGiao);
        if (rest == null) {
            return new String[0];
        }
        List<String> parts = new ArrayList<>();
        for (String p : rest.split(",")) {
            String t = p != null ? p.trim() : "";
            if (!t.isEmpty()) {
                parts.add(t);
            }
        }
        return parts.toArray(new String[0]);
    }

    private static boolean coGiaTri(String value) {
        return value != null && !value.isBlank();
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

    private static boolean duDiaChiGhn(HoaDon hoaDon) {
        if (coGiaTri(hoaDon.getGhnProvinceName()) && coGiaTri(hoaDon.getGhnWardName())) {
            return true;
        }
        if (ShippingService.looksLikeNewWardCode(hoaDon.getGhnWardCode())) {
            return true;
        }
        return hoaDon.getGhnDistrictId() != null
                && hoaDon.getGhnWardCode() != null
                && !hoaDon.getGhnWardCode().isBlank();
    }

    private static boolean canTaoVanDonGhn(TrangThaiDonHang trangThai) {
        return trangThai == TrangThaiDonHang.DA_XAC_NHAN
                || trangThai == TrangThaiDonHang.DANG_CHUAN_BI
                || trangThai == TrangThaiDonHang.DANG_GIAO;
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
