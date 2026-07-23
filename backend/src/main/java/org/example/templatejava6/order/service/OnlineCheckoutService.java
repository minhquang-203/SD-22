package org.example.templatejava6.order.service;

import org.example.templatejava6.cart.entity.ChiTietGioHang;
import org.example.templatejava6.cart.entity.GioHang;
import org.example.templatejava6.cart.repository.ChiTietGioHangRepository;
import org.example.templatejava6.cart.repository.GioHangRepository;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.example.templatejava6.order.model.request.HuyDonOnlineRequest;
import org.example.templatejava6.order.model.request.OnlineCheckoutRequest;
import org.example.templatejava6.order.model.request.OnlineTinhGiaRequest;
import org.example.templatejava6.order.model.response.HoaDonChiTietResponse;
import org.example.templatejava6.order.model.response.HoaDonDetailResponse;
import org.example.templatejava6.order.model.response.HoaDonResponse;
import org.example.templatejava6.order.model.response.OnlineCheckoutResponse;
import org.example.templatejava6.order.model.response.OnlineTinhGiaResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.PhuongThucThanhToanRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.OrderMailService;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.payment.model.request.TaoThanhToanRequest;
import org.example.templatejava6.payment.model.response.TaoThanhToanResponse;
import org.example.templatejava6.payment.service.PaymentService;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.realtime.service.OrderRealtimeService;
import org.example.templatejava6.shipping.model.request.ShippingFeeRequest;
import org.example.templatejava6.shipping.model.response.ShippingFeeResponse;
import org.example.templatejava6.shipping.service.ShippingService;
import org.example.templatejava6.voucher.model.response.VariantSaleInfo;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OnlineCheckoutService {

    private static final String LOAI_DON_ONLINE = "ONLINE";
    private static final String MA_COD = "COD";
    private static final String MA_VNPAY = "VNPAY";
    private static final String TRANG_THAI_CHO_THANH_TOAN = "CHO_THANH_TOAN";

    private final GioHangRepository gioHangRepository;
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final LoHangService loHangService;
    private final PaymentService paymentService;
    private final OnlineOrderLifecycleService onlineOrderLifecycleService;
    private final CheckoutPricingService checkoutPricingService;
    private final ThongBaoService thongBaoService;
    private final OrderMailService orderMailService;
    private final OrderRealtimeService orderRealtimeService;
    private final ShippingService shippingService;

    public OnlineCheckoutService(
            GioHangRepository gioHangRepository,
            ChiTietGioHangRepository chiTietGioHangRepository,
            KhachHangRepository khachHangRepository,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            PhuongThucThanhToanRepository phuongThucThanhToanRepository,
            ThanhToanHoaDonRepository thanhToanHoaDonRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            LoHangService loHangService,
            PaymentService paymentService,
            OnlineOrderLifecycleService onlineOrderLifecycleService,
            CheckoutPricingService checkoutPricingService,
            ThongBaoService thongBaoService,
            OrderMailService orderMailService,
            OrderRealtimeService orderRealtimeService,
            ShippingService shippingService) {
        this.gioHangRepository = gioHangRepository;
        this.chiTietGioHangRepository = chiTietGioHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.thanhToanHoaDonRepository = thanhToanHoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.loHangService = loHangService;
        this.paymentService = paymentService;
        this.onlineOrderLifecycleService = onlineOrderLifecycleService;
        this.checkoutPricingService = checkoutPricingService;
        this.thongBaoService = thongBaoService;
        this.orderMailService = orderMailService;
        this.orderRealtimeService = orderRealtimeService;
        this.shippingService = shippingService;
    }

    @Transactional
    public OnlineCheckoutResponse checkout(OnlineCheckoutRequest request, String clientIp) {
        KhachHang khachHang = getKhachDangNhap();
        String maPhuongThuc = normalize(request.getMaPhuongThucThanhToan());
        if (!MA_COD.equals(maPhuongThuc) && !MA_VNPAY.equals(maPhuongThuc)) {
            throw new ApiException("Chỉ hỗ trợ COD hoặc VNPAY cho bán hàng online.", "UNSUPPORTED_PAYMENT_METHOD");
        }
        PhuongThucThanhToan phuongThuc = resolvePhuongThuc(maPhuongThuc);

        GioHang gioHang = gioHangRepository.findFirstByKhachHang_IdOrderByIdAsc(khachHang.getId())
                .orElseThrow(() -> new ApiException("Khách hàng chưa có giỏ hàng.", "EMPTY_CART"));
        List<ChiTietGioHang> selectedItems = resolveSelectedCartItems(gioHang, request.getIdsChiTietGioHang());

        boolean isVnpay = MA_VNPAY.equals(maPhuongThuc);
        if (isVnpay) {
            // Hủy đơn VNPAY chưa trả trước đó (trước khi tạo đơn mới) để tránh giữ tồn chồng chéo.
            huyDonVnpayChuaThanhToanCuaKhach(khachHang.getId());
        }

        Map<Integer, VariantSaleInfo> saleMap = checkoutPricingService.loadActiveSales();
        List<LineCalc> lines = buildLines(selectedItems, saleMap);
        BigDecimal tongTien = sumTongTien(lines);

        PhieuGiamGia phieu = resolvePhieu(request.getMaPhieuGiamGia());
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        if (phieu != null) {
            tienGiamGia = checkoutPricingService.tinhTienGiamPhieu(phieu, tongTien);
        }
        BigDecimal phiVanChuyen = resolvePhiVanChuyen(
                request.getToDistrictId(), request.getToWardCode(), tongTien);
        BigDecimal thanhTien = tongTien.subtract(tienGiamGia).add(phiVanChuyen);
        if (thanhTien.compareTo(BigDecimal.ZERO) < 0) {
            thanhTien = BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(sinhMaHoaDon(now));
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdPhuongThucThanhToan(phuongThuc);
        hoaDon.setIdPhieuGiamGia(phieu);
        hoaDon.setLoaiDon(LOAI_DON_ONLINE);
        hoaDon.setTrangThai(TrangThaiDonHang.CHO_XAC_NHAN);
        hoaDon.setDiaChiGiao(request.getDiaChiGiao().trim());
        hoaDon.setTenNguoiNhan(coGiaTri(request.getTenNguoiNhan()) ? request.getTenNguoiNhan().trim() : khachHang.getHoTen());
        hoaDon.setSdtNguoiNhan(coGiaTri(request.getSdtNguoiNhan()) ? request.getSdtNguoiNhan().trim() : khachHang.getSoDienThoai());
        hoaDon.setGhnDistrictId(request.getToDistrictId());
        hoaDon.setGhnWardCode(coGiaTri(request.getToWardCode()) ? request.getToWardCode().trim() : null);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTienGiamGia(tienGiamGia);
        hoaDon.setPhiVanChuyen(phiVanChuyen);
        hoaDon.setThanhTien(thanhTien);
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setNgayTao(now);
        hoaDon = hoaDonRepository.save(hoaDon);

        for (LineCalc line : lines) {
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setIdHoaDon(hoaDon);
            chiTiet.setIdChiTietSanPham(line.chiTietSanPham());
            chiTiet.setSoLuong(line.soLuong());
            chiTiet.setDonGia(line.donGia());
            chiTiet.setThanhTien(line.thanhTien());
            hoaDonChiTietRepository.save(chiTiet);
            loHangService.truTonVaGhiNhan(chiTiet, line.soLuong());
        }

        if (phieu != null) {
            phieu.setSoLuong(phieu.getSoLuong() - 1);
            phieuGiamGiaRepository.save(phieu);
        }

        // COD: trừ giỏ ngay. VNPAY: giữ giỏ nguyên đến khi thanh toán thành công.
        if (!isVnpay) {
            chiTietGioHangRepository.deleteAll(selectedItems);
        }
        ghiNhatKy(hoaDon, "TAO_DON", "Khách hàng tạo đơn online từ " + selectedItems.size() + " sản phẩm đã chọn");
        ghiNhatKy(hoaDon, "TRU_TON", "Đã giữ hàng cho đơn online");

        TaoThanhToanResponse payment = null;
        if (MA_COD.equals(maPhuongThuc)) {
            taoThanhToanCod(hoaDon, phuongThuc, now);
            ghiNhatKy(hoaDon, "CHO_XAC_NHAN", "Đơn COD chờ nhân viên xác nhận");
            // COD: đơn đã đặt thành công ngay -> báo admin + gửi hóa đơn điện tử cho khách
            thongBaoDonMoi(hoaDon);
            orderMailService.guiHoaDonDatHangThanhCong(hoaDon);
            orderRealtimeService.publishCreated(hoaDon);
        } else {
            TaoThanhToanRequest paymentRequest = new TaoThanhToanRequest();
            paymentRequest.setIdHoaDon(hoaDon.getId());
            payment = paymentService.taoThanhToan(MA_VNPAY, paymentRequest, clientIp);
        }
        return OnlineCheckoutResponse.from(hoaDon, payment);
    }

    @Transactional(readOnly = true)
    public OnlineTinhGiaResponse tinhGia(OnlineTinhGiaRequest request) {
        KhachHang khachHang = getKhachDangNhap();
        GioHang gioHang = gioHangRepository.findFirstByKhachHang_IdOrderByIdAsc(khachHang.getId())
                .orElseThrow(() -> new ApiException("Khách hàng chưa có giỏ hàng.", "EMPTY_CART"));
        List<ChiTietGioHang> selectedItems = resolveSelectedCartItems(gioHang, request.getIdsChiTietGioHang());
        Map<Integer, VariantSaleInfo> saleMap = checkoutPricingService.loadActiveSales();
        List<LineCalc> lines = buildLines(selectedItems, saleMap);
        BigDecimal tongTien = sumTongTien(lines);

        PhieuGiamGia phieu = resolvePhieu(request.getMaPhieuGiamGia());
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        String maPhieu = null;
        if (phieu != null) {
            tienGiamGia = checkoutPricingService.tinhTienGiamPhieu(phieu, tongTien);
            maPhieu = phieu.getMa();
        }

        BigDecimal phiVanChuyen = resolvePhiVanChuyen(
                request.getToDistrictId(), request.getToWardCode(), tongTien);
        BigDecimal thanhTien = tongTien.subtract(tienGiamGia).add(phiVanChuyen);
        if (thanhTien.compareTo(BigDecimal.ZERO) < 0) {
            thanhTien = BigDecimal.ZERO;
        }

        OnlineTinhGiaResponse res = new OnlineTinhGiaResponse();
        res.setTongTien(tongTien);
        res.setTienGiamGia(tienGiamGia);
        res.setPhiVanChuyen(phiVanChuyen);
        res.setThanhTien(thanhTien);
        res.setMaPhieuGiamGia(maPhieu);
        return res;
    }

    @Transactional(readOnly = true)
    public List<HoaDonResponse> danhSachDonHang() {
        KhachHang khachHang = getKhachDangNhap();
        return hoaDonRepository.findByIdKhachHang_IdAndLoaiDonOrderByNgayTaoDesc(khachHang.getId(), LOAI_DON_ONLINE)
                .stream()
                // VNPAY chưa thanh toán: coi như chưa đặt — không hiện ở danh sách khách.
                .filter(hd -> !onlineOrderLifecycleService.laVnpayChuaThanhToan(hd))
                .map(HoaDonResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public HoaDonDetailResponse chiTietDonHang(Integer idHoaDon) {
        KhachHang khachHang = getKhachDangNhap();
        HoaDon hoaDon = loadOwnedOnlineOrder(khachHang.getId(), idHoaDon);
        if (onlineOrderLifecycleService.laVnpayChuaThanhToan(hoaDon)) {
            throw new ApiException("Không tìm thấy đơn hàng online.", "NOT_FOUND");
        }
        return buildDetailResponse(hoaDon);
    }

    @Transactional
    public HoaDonDetailResponse huyDonHang(Integer idHoaDon, HuyDonOnlineRequest request) {
        KhachHang khachHang = getKhachDangNhap();
        HoaDon hoaDon = loadOwnedOnlineOrder(khachHang.getId(), idHoaDon);
        String ghiChu = request != null ? request.getGhiChu() : null;
        onlineOrderLifecycleService.huyDonOnline(hoaDon,
                ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Khách hàng hủy đơn online");
        return buildDetailResponse(hoaDon);
    }

    private List<ChiTietGioHang> resolveSelectedCartItems(GioHang gioHang, List<Integer> idsChiTietGioHang) {
        Set<Integer> selectedIds = new LinkedHashSet<>(idsChiTietGioHang);
        List<ChiTietGioHang> cartItems = chiTietGioHangRepository.findByGioHangIdWithDetail(gioHang.getId());
        if (cartItems.isEmpty()) {
            throw new ApiException("Giỏ hàng trống. Vui lòng thêm sản phẩm.", "EMPTY_CART");
        }
        Map<Integer, ChiTietGioHang> cartById = new LinkedHashMap<>();
        for (ChiTietGioHang item : cartItems) {
            cartById.put(item.getId(), item);
        }
        List<ChiTietGioHang> selected = selectedIds.stream()
                .map(cartById::get)
                .filter(item -> item != null)
                .toList();
        if (selected.size() != selectedIds.size()) {
            throw new ApiException(
                    "Một hoặc nhiều sản phẩm đã chọn không còn trong giỏ hàng.",
                    "INVALID_CART_ITEMS");
        }
        return selected;
    }

    private List<LineCalc> buildLines(List<ChiTietGioHang> cartItems, Map<Integer, VariantSaleInfo> saleMap) {
        if (cartItems.isEmpty()) {
            throw new ApiException("Vui lòng chọn ít nhất một sản phẩm để thanh toán.", "EMPTY_SELECTION");
        }
        Map<Integer, LineCalc> lines = new LinkedHashMap<>();
        for (ChiTietGioHang item : cartItems) {
            ChiTietSanPham chiTietSanPham = item.getChiTietSanPham();
            validateChiTietSanPham(chiTietSanPham, item.getSoLuong());
            int soLuong = item.getSoLuong();
            BigDecimal donGia = checkoutPricingService.resolveDonGia(chiTietSanPham, saleMap);
            if (donGia == null || donGia.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Giá bán SKU " + chiTietSanPham.getSku() + " không hợp lệ.", "INVALID_PRICE");
            }
            LineCalc existing = lines.get(chiTietSanPham.getId());
            if (existing != null) {
                soLuong += existing.soLuong();
            }
            int ton = chiTietSanPham.getSoLuongTon() != null ? chiTietSanPham.getSoLuongTon() : 0;
            if (ton < soLuong) {
                throw new ApiException(
                        "Không đủ tồn cho SKU " + chiTietSanPham.getSku() + " (còn " + ton + ").",
                        "OUT_OF_STOCK");
            }
            lines.put(chiTietSanPham.getId(), new LineCalc(
                    chiTietSanPham,
                    soLuong,
                    donGia,
                    donGia.multiply(BigDecimal.valueOf(soLuong))));
        }
        return List.copyOf(lines.values());
    }

    private void validateChiTietSanPham(ChiTietSanPham chiTietSanPham, Integer soLuong) {
        if (soLuong == null || soLuong <= 0) {
            throw new ApiException("Số lượng sản phẩm không hợp lệ.", "INVALID_QTY");
        }
        if (!Boolean.TRUE.equals(chiTietSanPham.getTrangThai())) {
            throw new ApiException("SKU " + chiTietSanPham.getSku() + " không còn bán.", "INACTIVE_SKU");
        }
        if (chiTietSanPham.getSanPham() != null
                && !Boolean.TRUE.equals(chiTietSanPham.getSanPham().getTrangThai())) {
            throw new ApiException("Sản phẩm không còn bán.", "INACTIVE_PRODUCT");
        }
        if (chiTietSanPham.getGiaBan() == null || chiTietSanPham.getGiaBan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Giá bán SKU " + chiTietSanPham.getSku() + " không hợp lệ.", "INVALID_PRICE");
        }
    }

    private PhuongThucThanhToan resolvePhuongThuc(String maPhuongThuc) {
        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository.findByMaIgnoreCase(maPhuongThuc)
                .orElseThrow(() -> new ApiException(
                        "Chưa cấu hình phương thức thanh toán " + maPhuongThuc + ".", "PAYMENT_METHOD_NOT_FOUND"));
        if (!Boolean.TRUE.equals(phuongThuc.getTrangThai())) {
            throw new ApiException("Phương thức thanh toán không còn hoạt động.", "INACTIVE_PAYMENT");
        }
        return phuongThuc;
    }

    private PhieuGiamGia resolvePhieu(String maPhieuGiamGia) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.isBlank()) {
            return null;
        }
        return phieuGiamGiaRepository.findByMa(maPhieuGiamGia.trim())
                .orElseThrow(() -> new ApiException("Mã giảm giá không tồn tại.", "INVALID_VOUCHER"));
    }

    private void taoThanhToanCod(HoaDon hoaDon, PhuongThucThanhToan phuongThuc, LocalDateTime now) {
        ThanhToanHoaDon thanhToan = new ThanhToanHoaDon();
        thanhToan.setIdHoaDon(hoaDon);
        thanhToan.setIdPhuongThucThanhToan(phuongThuc);
        thanhToan.setSoTien(hoaDon.getThanhTien());
        thanhToan.setMaGiaoDich("COD-" + hoaDon.getId());
        thanhToan.setTrangThai(TRANG_THAI_CHO_THANH_TOAN);
        thanhToan.setThoiGian(now);
        thanhToanHoaDonRepository.save(thanhToan);
    }

    private BigDecimal sumTongTien(List<LineCalc> lines) {
        return lines.stream()
                .map(LineCalc::thanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HoaDon loadOwnedOnlineOrder(Integer idKhachHang, Integer idHoaDon) {
        return hoaDonRepository.findByIdAndIdKhachHang_IdAndLoaiDon(idHoaDon, idKhachHang, LOAI_DON_ONLINE)
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng online.", "NOT_FOUND"));
    }

    private HoaDonDetailResponse buildDetailResponse(HoaDon hoaDon) {
        HoaDonDetailResponse response = new HoaDonDetailResponse(hoaDon);
        response.setChiTiets(hoaDonChiTietRepository.findByIdHoaDon(hoaDon)
                .stream()
                .map(HoaDonChiTietResponse::new)
                .toList());
        thanhToanHoaDonRepository.findLatestByHoaDon(hoaDon).ifPresent(thanhToan -> {
            response.setSoTienKhachDua(thanhToan.getSoTienKhachDua());
            response.setTienThua(thanhToan.getTienThua());
            response.setMaGiaoDich(thanhToan.getMaGiaoDich());
        });
        return response;
    }

    private KhachHang getKhachDangNhap() {
        Integer id = SecurityUtils.currentKhachHangId();
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy tài khoản khách hàng.", "NOT_FOUND"));
    }

    /**
     * Tính phí vận chuyển phía server qua GHN. Không tin phí từ client.
     * Khi thiếu địa chỉ GHN, ShippingService trả phí fallback theo cấu hình.
     */
    private BigDecimal resolvePhiVanChuyen(Integer toDistrictId, String toWardCode, BigDecimal tongTienHang) {
        ShippingFeeRequest feeRequest = new ShippingFeeRequest();
        feeRequest.setToDistrictId(toDistrictId);
        feeRequest.setToWardCode(coGiaTri(toWardCode) ? toWardCode.trim() : null);
        if (tongTienHang != null && tongTienHang.compareTo(BigDecimal.ZERO) > 0) {
            feeRequest.setInsuranceValue(tongTienHang.setScale(0, RoundingMode.HALF_UP).longValue());
        }
        ShippingFeeResponse fee = shippingService.calcFee(feeRequest);
        return BigDecimal.valueOf(fee.getTotal());
    }

    private String sinhMaHoaDon(LocalDateTime now) {
        String prefix = "HD" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 50; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String maHoaDon = prefix + suffix;
            if (!hoaDonRepository.existsByMaHoaDon(maHoaDon)) {
                return maHoaDon;
            }
        }
        throw new ApiException("Không thể sinh mã hóa đơn. Vui lòng thử lại.", "CODE_GEN_FAILED");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean coGiaTri(String value) {
        return value != null && !value.isBlank();
    }

    private void huyDonVnpayChuaThanhToanCuaKhach(Integer idKhachHang) {
        List<HoaDon> donCu = hoaDonRepository
                .findByIdKhachHang_IdAndLoaiDonOrderByNgayTaoDesc(idKhachHang, LOAI_DON_ONLINE);
        for (HoaDon cu : donCu) {
            if (onlineOrderLifecycleService.laVnpayChuaThanhToan(cu)) {
                onlineOrderLifecycleService.xoaDonChuaThanhToan(cu);
            }
        }
    }

    private void thongBaoDonMoi(HoaDon hoaDon) {
        String tenKhach = hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getHoTen() != null
                ? hoaDon.getIdKhachHang().getHoTen()
                : "Khách hàng";
        String noiDung = tenKhach + " vừa đặt đơn " + hoaDon.getMaHoaDon()
                + " trị giá " + dinhDangTien(hoaDon.getThanhTien()) + ".";
        thongBaoService.taoThongBao(
                LoaiThongBao.DON_HANG_MOI,
                "Đơn hàng online mới",
                noiDung,
                "/admin/hoa-don/chi-tiet/" + hoaDon.getId(),
                hoaDon.getId(),
                hoaDon.getMaHoaDon());
    }

    private String dinhDangTien(BigDecimal value) {
        BigDecimal v = value != null ? value : BigDecimal.ZERO;
        return String.format(Locale.US, "%,d", v.setScale(0, java.math.RoundingMode.HALF_UP).longValue())
                .replace(',', '.') + "\u20ab";
    }

    private void ghiNhatKy(HoaDon hoaDon, String trangThai, String ghiChu) {
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setIdHoaDon(hoaDon);
        lichSu.setTrangThai(trangThai);
        lichSu.setGhiChu(ghiChu);
        lichSu.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(lichSu);
    }

    private record LineCalc(
            ChiTietSanPham chiTietSanPham,
            int soLuong,
            BigDecimal donGia,
            BigDecimal thanhTien) {
    }
}
