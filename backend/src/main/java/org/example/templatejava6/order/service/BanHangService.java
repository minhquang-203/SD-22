package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.HoaDonChiTietLo;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.example.templatejava6.order.model.request.GiuDonChoRequest;
import org.example.templatejava6.order.model.request.PosTinhGiaRequest;
import org.example.templatejava6.order.model.request.TaoDonTaiQuayRequest;
import org.example.templatejava6.order.model.response.BanHangHoaDonResponse;
import org.example.templatejava6.order.model.response.BanHangHoaDonResponse.BanHangChiTietResponse;
import org.example.templatejava6.order.model.response.BienTheBanResponse;
import org.example.templatejava6.order.model.response.DonChoDetailResponse;
import org.example.templatejava6.order.model.response.DonChoListItemResponse;
import org.example.templatejava6.order.model.response.GiuDonChoResponse;
import org.example.templatejava6.order.model.response.PosTinhGiaResponse;
import org.example.templatejava6.order.model.response.PosThanhToanStatusResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietLoRepository;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.order.repository.PhuongThucThanhToanRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.repository.AnhSanPhamRepository;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.payment.model.request.TaoThanhToanRequest;
import org.example.templatejava6.payment.model.response.TaoThanhToanResponse;
import org.example.templatejava6.payment.service.PaymentService;
import org.example.templatejava6.payment.vnpay.VnpayGateway;
import org.example.templatejava6.voucher.model.response.VariantSaleInfo;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.example.templatejava6.voucher.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BanHangService {

    private static final String LOAI_TAI_QUAY = "TAI_QUAY";
    private static final String GUEST_PREFIX = "__POS_GUEST__|";
    private static final int MAX_HELD_ORDERS = 15;
    private static final String TRANG_THAI_THANH_CONG = "THANH_CONG";
    private static final String MA_TIEN_MAT = "TIEN_MAT";
    private static final String MA_VNPAY = "VNPAY";
    private static final String MA_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    private static final Set<String> SPLIT_ALLOWED_METHODS = Set.of(MA_TIEN_MAT, MA_CHUYEN_KHOAN);
    private static final String MA_COD = "COD";
    private static final String TRANG_THAI_CHO_THANH_TOAN = "CHO_THANH_TOAN";
    private static final String TRANG_THAI_THAT_BAI = "THAT_BAI";
    private static final int SAN_PHAM_PAGE_SIZE = 48;

    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private AnhSanPhamRepository anhSanPhamRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired private HoaDonChiTietLoRepository hoaDonChiTietLoRepository;
    @Autowired private ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    @Autowired private LichSuDonHangRepository lichSuDonHangRepository;
    @Autowired private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Autowired private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired private PhieuGiamGiaService phieuGiamGiaService;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private LoHangService loHangService;
    @Autowired private CheckoutPricingService checkoutPricingService;
    @Autowired private PaymentService paymentService;
    @Autowired private PosOrderLifecycleService posOrderLifecycleService;

    @Transactional(readOnly = true)
    public List<BienTheBanResponse> danhSachSanPhamBan(String keyword, Integer page) {
        String kw = keyword != null ? keyword.trim() : "";
        int pageNo = page != null && page >= 0 ? page : 0;
        Map<Integer, VariantSaleInfo> saleMap = checkoutPricingService.loadActiveSales();
        List<ChiTietSanPham> variants = chiTietSanPhamRepository
                .danhSachBienTheBan(kw, PageRequest.of(pageNo, SAN_PHAM_PAGE_SIZE));
        Map<Integer, String> imageMap = loadMainImageUrls(variants);
        return variants.stream()
                .map(cts -> toBienTheBanResponse(cts, saleMap, imageMap))
                .toList();
    }

    private Map<Integer, String> loadMainImageUrls(List<ChiTietSanPham> variants) {
        Set<Integer> sanPhamIds = variants.stream()
                .map(cts -> cts.getSanPham() != null ? cts.getSanPham().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (sanPhamIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<AnhSanPham>> byProduct = anhSanPhamRepository.findBySanPham_IdIn(sanPhamIds).stream()
                .collect(Collectors.groupingBy(a -> a.getSanPham().getId()));
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, List<AnhSanPham>> entry : byProduct.entrySet()) {
            entry.getValue().stream()
                    .sorted(Comparator
                            .comparing((AnhSanPham a) -> !Boolean.TRUE.equals(a.getLaAnhChinh()))
                            .thenComparing(a -> a.getThuTu() != null ? a.getThuTu() : 0))
                    .map(AnhSanPham::getUrl)
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .ifPresent(url -> result.put(entry.getKey(), url));
        }
        return result;
    }

    private BienTheBanResponse toBienTheBanResponse(
            ChiTietSanPham cts,
            Map<Integer, VariantSaleInfo> saleMap,
            Map<Integer, String> imageMap) {
        BienTheBanResponse res = new BienTheBanResponse(cts);
        BigDecimal donGia = checkoutPricingService.resolveDonGia(cts, saleMap);
        if (donGia != null) {
            res.setGiaBan(donGia);
        }
        VariantSaleInfo sale = saleMap != null ? saleMap.get(cts.getId()) : null;
        if (sale != null && sale.getGiaSauGiam() != null) {
            res.setDangGiamGia(true);
            res.setGiaGoc(sale.getGiaGoc() != null ? sale.getGiaGoc() : cts.getGiaBan());
            res.setPhanTramGiam(sale.getPhanTramGiam());
        } else {
            res.setDangGiamGia(false);
        }
        if (cts.getSanPham() != null) {
            res.setAnhUrl(imageMap.get(cts.getSanPham().getId()));
        }
        LocalDate nearest = loHangService.nearestExpiry(cts.getId());
        res.setHanSuDungGanNhat(nearest);
        if (nearest != null) {
            res.setSoNgayConLai((int) ChronoUnit.DAYS.between(LocalDate.now(), nearest));
        }
        return res;
    }

    @Transactional(readOnly = true)
    public List<BienTheBanResponse> timSanPhamBan(String keyword) {
        return danhSachSanPhamBan(keyword, 0);
    }

    @Transactional(readOnly = true)
    public PosTinhGiaResponse tinhGiaTaiQuay(PosTinhGiaRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ApiException("Giỏ hàng trống.", "EMPTY_CART");
        }

        Map<Integer, Integer> qtyByVariant = mergeItems(req.getItems());
        Map<Integer, VariantSaleInfo> saleMap = checkoutPricingService.loadActiveSales();
        List<LineCalc> lines = buildLines(qtyByVariant, false, saleMap);
        BigDecimal tongTien = sumTongTien(lines);

        BigDecimal tienGiamGia = BigDecimal.ZERO;
        String maPhieu = null;
        if (req.getMaPhieuGiamGia() != null && !req.getMaPhieuGiamGia().isBlank()) {
            final String maPhieuTrimmed = req.getMaPhieuGiamGia().trim();
            maPhieu = maPhieuTrimmed;
            PhieuGiamGia phieu = phieuGiamGiaRepository.findByMa(maPhieuTrimmed)
                    .orElseThrow(() -> new ApiException(
                            "Mã giảm giá \"" + maPhieuTrimmed + "\" không tồn tại.", "INVALID_VOUCHER"));
            tienGiamGia = checkoutPricingService.tinhTienGiamPhieu(phieu, tongTien);
        }

        BigDecimal thanhTien = tongTien.subtract(tienGiamGia);
        if (thanhTien.compareTo(BigDecimal.ZERO) < 0) {
            thanhTien = BigDecimal.ZERO;
        }

        PosTinhGiaResponse res = new PosTinhGiaResponse();
        res.setTongTien(tongTien);
        res.setTienGiamGia(tienGiamGia);
        res.setThanhTien(thanhTien);
        res.setMaPhieuGiamGia(maPhieu);
        return res;
    }

    @Transactional
    public GiuDonChoResponse giuDonCho(GiuDonChoRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ApiException("Giỏ hàng trống. Không thể giữ đơn.", "EMPTY_CART");
        }

        long soDonCho = hoaDonRepository.countByTrangThaiAndLoaiDon(TrangThaiDonHang.CHO, LOAI_TAI_QUAY);
        if (soDonCho >= MAX_HELD_ORDERS) {
            throw new ApiException("Đã đạt tối đa 15 hóa đơn chờ", "MAX_HELD_ORDERS");
        }

        Map<Integer, VariantSaleInfo> saleMap = checkoutPricingService.loadActiveSales();
        // Giữ đơn phải kiểm tra và trừ tồn theo lô (FEFO hoặc lô chọn), giống checkout.
        List<LineCalc> lines = buildLinesWithLots(req.getItems(), true, saleMap);
        BigDecimal tongTien = sumTongTien(lines);

        KhachHang khachHang = resolveKhachHang(req.getIdKhachHang());
        NhanVien nhanVien = currentNhanVien();

        LocalDateTime now = LocalDateTime.now();
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(sinhMaHoaDon(now));
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdNhanVien(nhanVien);
        hoaDon.setLoaiDon(LOAI_TAI_QUAY);
        hoaDon.setTrangThai(TrangThaiDonHang.CHO);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTienGiamGia(BigDecimal.ZERO);
        hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
        hoaDon.setThanhTien(tongTien);
        hoaDon.setGhiChu(buildGhiChuChoDon(khachHang, req));
        hoaDon.setNgayTao(now);
        hoaDon = hoaDonRepository.save(hoaDon);

        saveChiTietLines(hoaDon, lines);
        return new GiuDonChoResponse(hoaDon.getId(), hoaDon.getMaHoaDon());
    }

    @Transactional(readOnly = true)
    public List<DonChoListItemResponse> danhSachDonCho() {
        return hoaDonRepository
                .findByTrangThaiAndLoaiDonOrderByNgayTaoDesc(TrangThaiDonHang.CHO, LOAI_TAI_QUAY)
                .stream()
                .map(hd -> {
                    int soMatHang = hoaDonChiTietRepository.findByIdHoaDon(hd).stream()
                            .mapToInt(line -> line.getSoLuong() != null ? line.getSoLuong() : 0)
                            .sum();
                    return new DonChoListItemResponse(hd, soMatHang);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DonChoDetailResponse chiTietDonCho(Integer id) {
        HoaDon hd = loadDonCho(id);
        DonChoDetailResponse res = new DonChoDetailResponse();
        res.setId(hd.getId());
        if (hd.getIdKhachHang() != null) {
            res.setIdKhachHang(hd.getIdKhachHang().getId());
            res.setHoTenKhachHang(hd.getIdKhachHang().getHoTen());
            res.setSoDienThoai(hd.getIdKhachHang().getSoDienThoai());
        } else {
            String[] guest = parseGuestGhiChu(hd.getGhiChu());
            if (guest != null) {
                res.setHoTenKhachHang(guest[0]);
                res.setSoDienThoai(guest[1]);
            }
        }
        List<DonChoDetailResponse.DonChoLineResponse> items = new ArrayList<>();
        for (HoaDonChiTiet line : hoaDonChiTietRepository.findByIdHoaDon(hd)) {
            ChiTietSanPham cts = line.getIdChiTietSanPham();
            if (cts == null) {
                // Dòng thiếu biến thể — bỏ qua, không để NPE sập POS
                continue;
            }
            DonChoDetailResponse.DonChoLineResponse item = new DonChoDetailResponse.DonChoLineResponse();
            item.setIdChiTietSanPham(cts.getId());
            item.setSku(cts.getSku());
            item.setTenSanPham(cts.getSanPham() != null ? cts.getSanPham().getTen() : null);
            item.setDungTichMl(cts.getDungTichMl());
            item.setTenMauSac(cts.getMauSac() != null ? cts.getMauSac().getTen() : null);
            item.setDonGia(line.getDonGia());
            item.setSoLuong(line.getSoLuong() != null ? line.getSoLuong() : 0);
            item.setSoLuongTon(cts.getSoLuongTon() != null ? cts.getSoLuongTon() : 0);
            List<HoaDonChiTietLo> lotRows = hoaDonChiTietLoRepository.findByHoaDonChiTiet(line);
            if (lotRows != null && !lotRows.isEmpty()) {
                List<DonChoDetailResponse.PhanBoLoItem> phanBo = new ArrayList<>();
                for (HoaDonChiTietLo row : lotRows) {
                    if (row.getLoHang() == null) {
                        continue;
                    }
                    DonChoDetailResponse.PhanBoLoItem pb = new DonChoDetailResponse.PhanBoLoItem();
                    pb.setIdLoHang(row.getLoHang().getId());
                    pb.setSoLo(row.getLoHang().getSoLo());
                    pb.setSoLuong(row.getSoLuong());
                    pb.setHanSuDung(row.getLoHang().getHanSuDung());
                    phanBo.add(pb);
                }
                if (!phanBo.isEmpty()) {
                    item.setPhanBoLos(phanBo);
                    if (phanBo.size() == 1) {
                        item.setIdLoHang(phanBo.get(0).getIdLoHang());
                        item.setSoLo(phanBo.get(0).getSoLo());
                    }
                }
            }
            items.add(item);
        }
        res.setItems(items);
        return res;
    }

    @Transactional
    public void huyDonCho(Integer id) {
        HoaDon hd = loadDonCho(id);
        hoanTonTheoHoaDon(hd);
        hoaDonChiTietRepository.deleteByIdHoaDon(hd);
        hoaDonRepository.delete(hd);
    }

    @Transactional
    public BanHangHoaDonResponse taoDonTaiQuay(TaoDonTaiQuayRequest req, String clientIp) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ApiException("Giỏ hàng trống. Vui lòng thêm sản phẩm.", "EMPTY_CART");
        }
        boolean isSplitPayment = req.getDanhSachThanhToan() != null && !req.getDanhSachThanhToan().isEmpty();
        if (req.getIdPhuongThucThanhToan() == null) {
            throw new ApiException("Vui lòng chọn phương thức thanh toán.", "MISSING_PAYMENT");
        }

        PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(req.getIdPhuongThucThanhToan())
                .orElseThrow(() -> new ApiException("Phương thức thanh toán không hợp lệ.", "INVALID_PAYMENT"));
        if (!Boolean.TRUE.equals(pttt.getTrangThai())) {
            throw new ApiException("Phương thức thanh toán không còn hoạt động.", "INACTIVE_PAYMENT");
        }
        if (MA_COD.equalsIgnoreCase(pttt.getMa())) {
            throw new ApiException(
                    "Thanh toán khi nhận hàng không áp dụng cho bán tại quầy.",
                    "UNSUPPORTED_PAYMENT_METHOD");
        }

        Map<Integer, VariantSaleInfo> saleMap = checkoutPricingService.loadActiveSales();
        List<LineCalc> lines = buildLinesWithLots(req.getItems(), true, saleMap);
        BigDecimal tongTien = sumTongTien(lines);

        PhieuGiamGia phieu = null;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        if (req.getMaPhieuGiamGia() != null && !req.getMaPhieuGiamGia().isBlank()) {
            phieu = phieuGiamGiaRepository.findByMa(req.getMaPhieuGiamGia().trim())
                    .orElseThrow(() -> new ApiException(
                            "Mã giảm giá \"" + req.getMaPhieuGiamGia() + "\" không tồn tại.", "INVALID_VOUCHER"));
            tienGiamGia = checkoutPricingService.tinhTienGiamPhieu(phieu, tongTien);
        }

        BigDecimal thanhTien = tongTien.subtract(tienGiamGia);
        if (thanhTien.compareTo(BigDecimal.ZERO) < 0) {
            thanhTien = BigDecimal.ZERO;
        }

        boolean isVnpay = MA_VNPAY.equals(pttt.getMa());
        if (isVnpay && thanhTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(
                    "Đơn miễn phí không thể thanh toán qua VNPAY. Vui lòng chọn tiền mặt hoặc chuyển khoản.",
                    "INVALID_PAYMENT_AMOUNT");
        }

        BigDecimal soTienKhachDua = null;
        BigDecimal tienThua = null;
        if (!isSplitPayment && MA_TIEN_MAT.equals(pttt.getMa())) {
            if (req.getSoTienKhachDua() == null) {
                throw new ApiException("Vui lòng nhập tiền khách đưa.", "MISSING_CASH");
            }
            soTienKhachDua = req.getSoTienKhachDua();
            if (soTienKhachDua.compareTo(thanhTien) < 0) {
                throw new ApiException("Tiền khách đưa không đủ thanh toán.", "INSUFFICIENT_CASH");
            }
            tienThua = soTienKhachDua.subtract(thanhTien);
        }

        KhachHang khachHang = resolveKhachHang(req.getIdKhachHang());
        NhanVien nhanVien = currentNhanVien();
        LocalDateTime now = LocalDateTime.now();

        HoaDon hoaDon;
        if (req.getIdHoaDonCho() != null) {
            hoaDon = loadDonCho(req.getIdHoaDonCho());
            // Đơn chờ đã trừ tồn khi giữ — hoàn lại trước khi trừ theo dòng mới (tránh trừ 2 lần).
            hoanTonTheoHoaDon(hoaDon);
            hoaDonChiTietRepository.deleteByIdHoaDon(hoaDon);
        } else {
            hoaDon = new HoaDon();
            hoaDon.setMaHoaDon(sinhMaHoaDon(now));
            hoaDon.setNgayTao(now);
        }

        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdNhanVien(nhanVien);
        hoaDon.setIdPhuongThucThanhToan(pttt);
        hoaDon.setIdPhieuGiamGia(phieu);
        hoaDon.setLoaiDon(LOAI_TAI_QUAY);
        hoaDon.setTrangThai(isVnpay ? TrangThaiDonHang.CHO_XAC_NHAN : TrangThaiDonHang.HOAN_THANH);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTienGiamGia(tienGiamGia);
        hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
        hoaDon.setThanhTien(thanhTien);
        hoaDon.setGhiChu(req.getGhiChu());
        hoaDon = hoaDonRepository.save(hoaDon);

        String tenKhach = khachHang != null ? khachHang.getHoTen() : "Khách lẻ";
        ghiNhatKy(hoaDon, "TAO_DON", "Tạo hóa đơn (" + tenKhach + ")", nhanVien, now);

        List<BanHangChiTietResponse> lineResponses = new ArrayList<>();
        for (LineCalc line : lines) {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdChiTietSanPham(line.cts);
            hdct.setSoLuong(line.soLuong);
            hdct.setDonGia(line.donGia);
            hdct.setThanhTien(line.thanhTien);
            hdct = hoaDonChiTietRepository.save(hdct);

            loHangService.truTonVaGhiNhan(hdct, line.soLuong, line.idLoHang, line.phanBoLos);

            lineResponses.add(new BanHangChiTietResponse(hdct));
            ghiNhatKy(hoaDon, "THEM_HANG",
                    "Thêm " + tenSanPhamLine(line.cts) + " " + bienTheLine(line.cts) + " ×" + line.soLuong,
                    nhanVien, now);
        }

        if (phieu != null && tienGiamGia.compareTo(BigDecimal.ZERO) > 0) {
            ghiNhatKy(hoaDon, "AP_MA",
                    "Áp mã giảm giá " + phieu.getMa() + " (−" + tienGiamGia.toPlainString() + "đ)",
                    nhanVien, now);
        }
        if (isSplitPayment) {
            if (req.getDanhSachThanhToan().size() < 2) {
                throw new ApiException(
                        "Thanh toán kết hợp cần ít nhất 2 phương thức.",
                        "SPLIT_MIN_METHODS");
            }

            List<PhuongThucThanhToan> splitPts = new ArrayList<>();
            BigDecimal tongSplit = BigDecimal.ZERO;
            for (TaoDonTaiQuayRequest.ThanhToanItemRequest item : req.getDanhSachThanhToan()) {
                if (item.getIdPhuongThucThanhToan() == null || item.getSoTien() == null) {
                    throw new ApiException("Thiếu thông tin thanh toán.", "SPLIT_INVALID_ITEM");
                }
                if (item.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ApiException("Số tiền từng phần phải lớn hơn 0.", "SPLIT_INVALID_AMOUNT");
                }

                PhuongThucThanhToan ptItem = phuongThucThanhToanRepository
                        .findById(item.getIdPhuongThucThanhToan())
                        .orElseThrow(() -> new ApiException(
                                "Phương thức thanh toán không hợp lệ.", "INVALID_PAYMENT"));
                if (!Boolean.TRUE.equals(ptItem.getTrangThai())) {
                    throw new ApiException(
                            "Phương thức thanh toán không còn hoạt động.", "INACTIVE_PAYMENT");
                }
                if (!SPLIT_ALLOWED_METHODS.contains(ptItem.getMa())) {
                    throw new ApiException(
                            "Thanh toán kết hợp chỉ hỗ trợ Tiền mặt và Chuyển khoản.",
                            "SPLIT_UNSUPPORTED_METHOD");
                }
                tongSplit = tongSplit.add(item.getSoTien());
                splitPts.add(ptItem);
            }

            if (tongSplit.compareTo(thanhTien) != 0) {
                throw new ApiException(
                        "Tổng thanh toán (" + tongSplit.toPlainString()
                                + "đ) không khớp thành tiền (" + thanhTien.toPlainString() + "đ).",
                        "SPLIT_TOTAL_MISMATCH");
            }

            PhuongThucThanhToan ptttLonNhat = null;
            BigDecimal soTienLonNhat = BigDecimal.valueOf(-1);
            ThanhToanHoaDon ttDaiDien = null;
            List<ThanhToanHoaDon> cacDongThanhToan = new ArrayList<>();

            for (int i = 0; i < req.getDanhSachThanhToan().size(); i++) {
                TaoDonTaiQuayRequest.ThanhToanItemRequest item = req.getDanhSachThanhToan().get(i);
                PhuongThucThanhToan ptItem = splitPts.get(i);

                ThanhToanHoaDon tt = new ThanhToanHoaDon();
                tt.setIdHoaDon(hoaDon);
                tt.setIdPhuongThucThanhToan(ptItem);
                tt.setSoTien(item.getSoTien());
                // Kết hợp: không tính tiền thối cho phần tiền mặt
                tt.setSoTienKhachDua(null);
                tt.setTienThua(null);
                tt.setMaGiaoDich(item.getMaGiaoDich());
                tt.setTrangThai(TRANG_THAI_THANH_CONG);
                tt.setThoiGian(now);
                tt = thanhToanHoaDonRepository.save(tt);
                cacDongThanhToan.add(tt);

                if (ttDaiDien == null || item.getSoTien().compareTo(soTienLonNhat) > 0) {
                    soTienLonNhat = item.getSoTien();
                    ptttLonNhat = ptItem;
                    ttDaiDien = tt;
                }

                ghiNhatKy(hoaDon, "THANH_TOAN",
                        "Thanh toán " + ptItem.getTen() + " — " + item.getSoTien().toPlainString() + "đ",
                        nhanVien, now);
            }

            if (ptttLonNhat != null) {
                hoaDon.setIdPhuongThucThanhToan(ptttLonNhat);
            }
            hoaDon.setTrangThai(TrangThaiDonHang.HOAN_THANH);
            hoaDon = hoaDonRepository.save(hoaDon);

            ghiNhatKy(hoaDon, "HOAN_THANH", "Hoàn thành đơn", nhanVien, now);

            if (phieu != null) {
                phieuGiamGiaService.consumeOne(phieu.getId());
            }

            if (khachHang != null) {
                int diemThem = thanhTien.divide(BigDecimal.valueOf(1000), 0, RoundingMode.FLOOR).intValue();
                int diemHien = khachHang.getDiemTichLuy() != null ? khachHang.getDiemTichLuy() : 0;
                khachHang.setDiemTichLuy(diemHien + diemThem);
                khachHangRepository.save(khachHang);
            }

            return BanHangHoaDonResponse.from(hoaDon, ttDaiDien, lineResponses, cacDongThanhToan);
        }

        if (isVnpay) {
            if (phieu != null) {
                phieuGiamGiaService.consumeOne(phieu.getId());
            }
            TaoThanhToanRequest paymentRequest = new TaoThanhToanRequest();
            paymentRequest.setIdHoaDon(hoaDon.getId());
            TaoThanhToanResponse payment = paymentService.taoThanhToan(VnpayGateway.PROVIDER_CODE, paymentRequest, clientIp);
            BanHangHoaDonResponse res = BanHangHoaDonResponse.from(hoaDon, null, lineResponses);
            res.setPaymentUrl(payment.getPaymentUrl());
            res.setTransactionRef(payment.getTransactionRef());
            res.setTrangThaiThanhToan(TRANG_THAI_CHO_THANH_TOAN);
            return res;
        }

        ThanhToanHoaDon tt = new ThanhToanHoaDon();
        tt.setIdHoaDon(hoaDon);
        tt.setIdPhuongThucThanhToan(pttt);
        tt.setSoTien(thanhTien);
        tt.setSoTienKhachDua(soTienKhachDua);
        tt.setTienThua(tienThua);
        tt.setMaGiaoDich(req.getMaGiaoDich());
        tt.setTrangThai(TRANG_THAI_THANH_CONG);
        tt.setThoiGian(now);
        tt = thanhToanHoaDonRepository.save(tt);

        ghiNhatKy(hoaDon, "THANH_TOAN",
                "Thanh toán " + pttt.getTen() + " — " + thanhTien.toPlainString() + "đ",
                nhanVien, now);
        ghiNhatKy(hoaDon, "HOAN_THANH", "Hoàn thành đơn", nhanVien, now);

        if (phieu != null) {
            phieuGiamGiaService.consumeOne(phieu.getId());
        }

        if (khachHang != null) {
            int diemThem = thanhTien.divide(BigDecimal.valueOf(1000), 0, RoundingMode.FLOOR).intValue();
            int diemHien = khachHang.getDiemTichLuy() != null ? khachHang.getDiemTichLuy() : 0;
            khachHang.setDiemTichLuy(diemHien + diemThem);
            khachHangRepository.save(khachHang);
        }

        return BanHangHoaDonResponse.from(hoaDon, tt, lineResponses);
    }

    @Transactional(readOnly = true)
    public PosThanhToanStatusResponse kiemTraThanhToanTaiQuay(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn.", "NOT_FOUND"));
        if (!LOAI_TAI_QUAY.equalsIgnoreCase(hoaDon.getLoaiDon())) {
            throw new ApiException("Không phải hóa đơn tại quầy.", "INVALID_ORDER_TYPE");
        }

        ThanhToanHoaDon thanhToan = thanhToanHoaDonRepository.findLatestByHoaDon(hoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy giao dịch thanh toán.", "PAYMENT_NOT_FOUND"));
        String trangThai = thanhToan.getTrangThai() != null ? thanhToan.getTrangThai() : TRANG_THAI_CHO_THANH_TOAN;

        BanHangHoaDonResponse hoaDonResponse = null;
        if (TRANG_THAI_THANH_CONG.equals(trangThai)) {
            List<BanHangChiTietResponse> lines = hoaDonChiTietRepository.findByIdHoaDon(hoaDon).stream()
                    .map(BanHangChiTietResponse::new)
                    .toList();
            List<ThanhToanHoaDon> tatCaThanhToan =
                    thanhToanHoaDonRepository.findByIdHoaDonOrderByThoiGianDesc(hoaDon);
            hoaDonResponse = BanHangHoaDonResponse.from(hoaDon, thanhToan, lines, tatCaThanhToan);
            hoaDonResponse.setTrangThaiThanhToan(TRANG_THAI_THANH_CONG);
        }

        return PosThanhToanStatusResponse.of(hoaDon.getId(), hoaDon.getMaHoaDon(), trangThai, hoaDonResponse);
    }

    @Transactional
    public void huyThanhToanVnpayTaiQuay(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn.", "NOT_FOUND"));
        posOrderLifecycleService.huyDonVnpay(hoaDon, "Nhân viên hủy thanh toán QR tại quầy");
        thanhToanHoaDonRepository.findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_CHO_THANH_TOAN)
                .ifPresent(thanhToan -> {
                    thanhToan.setTrangThai(TRANG_THAI_THAT_BAI);
                    thanhToan.setThoiGian(LocalDateTime.now());
                    thanhToanHoaDonRepository.save(thanhToan);
                });
    }

    /**
     * Hoàn tất thanh toán VNPAY tại quầy thủ công (khi chưa có IPN).
     * Nhân viên xác nhận khách đã quét QR / thanh toán thành công trên app.
     */
    @Transactional
    public PosThanhToanStatusResponse hoanTatThanhToanVnpayTaiQuay(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn.", "NOT_FOUND"));
        if (!LOAI_TAI_QUAY.equalsIgnoreCase(hoaDon.getLoaiDon())) {
            throw new ApiException("Không phải hóa đơn tại quầy.", "INVALID_ORDER_TYPE");
        }
        if (hoaDon.getTrangThai() == TrangThaiDonHang.HOAN_THANH) {
            return kiemTraThanhToanTaiQuay(idHoaDon);
        }
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai().laTrangThaiKetThuc()) {
            throw new ApiException("Đơn hàng đã kết thúc, không thể hoàn tất thanh toán.", "ORDER_CLOSED");
        }

        ThanhToanHoaDon thanhToan = thanhToanHoaDonRepository
                .findLatestByHoaDonAndTrangThai(hoaDon, TRANG_THAI_CHO_THANH_TOAN)
                .orElseThrow(() -> new ApiException(
                        "Không có giao dịch VNPAY đang chờ thanh toán.", "PAYMENT_NOT_PENDING"));

        NhanVien nhanVien = currentNhanVien();
        LocalDateTime now = LocalDateTime.now();

        thanhToan.setTrangThai(TRANG_THAI_THANH_CONG);
        thanhToan.setThoiGian(now);
        thanhToanHoaDonRepository.save(thanhToan);

        posOrderLifecycleService.hoanThanhDonVnpay(hoaDon);
        ghiNhatKy(hoaDon, "THANH_TOAN",
                "Nhân viên xác nhận thanh toán VNPAY tại quầy (không qua IPN)",
                nhanVien, now);

        List<BanHangChiTietResponse> lines = hoaDonChiTietRepository.findByIdHoaDon(hoaDon).stream()
                .map(BanHangChiTietResponse::new)
                .toList();
        List<ThanhToanHoaDon> tatCaThanhToan =
                thanhToanHoaDonRepository.findByIdHoaDonOrderByThoiGianDesc(hoaDon);
        BanHangHoaDonResponse hoaDonResponse =
                BanHangHoaDonResponse.from(hoaDon, thanhToan, lines, tatCaThanhToan);
        hoaDonResponse.setTrangThaiThanhToan(TRANG_THAI_THANH_CONG);
        return PosThanhToanStatusResponse.of(hoaDon.getId(), hoaDon.getMaHoaDon(), TRANG_THAI_THANH_CONG, hoaDonResponse);
    }

    private HoaDon loadDonCho(Integer id) {
        return hoaDonRepository
                .findByIdAndTrangThaiAndLoaiDon(id, TrangThaiDonHang.CHO, LOAI_TAI_QUAY)
                .orElseThrow(() -> new ApiException("Đơn chờ không tồn tại hoặc đã được xử lý.", "NOT_FOUND"));
    }

    private String buildGhiChuChoDon(KhachHang khachHang, GiuDonChoRequest req) {
        if (khachHang != null) {
            return blankToNull(req.getGhiChu());
        }
        String ten = blankToNull(req.getTenKhachHang());
        String sdt = blankToNull(req.getSoDienThoai());
        if (ten == null && sdt == null) {
            return blankToNull(req.getGhiChu());
        }
        String note = blankToNull(req.getGhiChu());
        return GUEST_PREFIX
                + (ten != null ? ten : "")
                + "|"
                + (sdt != null ? sdt : "")
                + (note != null ? "|" + note : "");
    }

    /** @return [ten, sdt] hoặc null */
    public static String[] parseGuestGhiChu(String ghiChu) {
        if (ghiChu == null || !ghiChu.startsWith(GUEST_PREFIX)) {
            return null;
        }
        String rest = ghiChu.substring(GUEST_PREFIX.length());
        String[] parts = rest.split("\\|", 3);
        String ten = parts.length > 0 && !parts[0].isBlank() ? parts[0].trim() : null;
        String sdt = parts.length > 1 && !parts[1].isBlank() ? parts[1].trim() : null;
        if (ten == null && sdt == null) {
            return null;
        }
        return new String[]{ten, sdt};
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private KhachHang resolveKhachHang(Integer idKhachHang) {
        if (idKhachHang == null) {
            return null;
        }
        return khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new ApiException("Khách hàng không tồn tại.", "NOT_FOUND"));
    }

    private NhanVien currentNhanVien() {
        Integer id = SecurityUtils.currentNhanVienId();
        NhanVien nv = nhanVienRepository.findByIdWithVaiTro(id)
                .orElseThrow(() -> new ApiException("Nhân viên không tồn tại.", "NOT_FOUND"));
        if (!Boolean.TRUE.equals(nv.getTrangThai())) {
            throw new ApiException("Tài khoản đã bị khóa", "ACCOUNT_DISABLED");
        }
        return nv;
    }

    private List<LineCalc> buildLines(
            Map<Integer, Integer> qtyByVariant,
            boolean checkStock,
            Map<Integer, VariantSaleInfo> saleMap) {
        List<LineCalc> lines = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : qtyByVariant.entrySet()) {
            lines.add(buildOneLine(entry.getKey(), entry.getValue(), null, checkStock, saleMap));
        }
        return lines;
    }

    /** Giữ/thanh toán POS: gộp theo (biến thể + lô chọn); null lô = FEFO; nhiều lô = 1 dòng riêng. */
    private List<LineCalc> buildLinesWithLots(
            List<TaoDonTaiQuayRequest.ItemRequest> items,
            boolean checkStock,
            Map<Integer, VariantSaleInfo> saleMap) {
        Map<String, MergedLotLine> merged = new LinkedHashMap<>();
        int multiSeq = 0;
        for (TaoDonTaiQuayRequest.ItemRequest item : items) {
            if (item.getIdChiTietSanPham() == null || item.getSoLuong() == null || item.getSoLuong() <= 0) {
                throw new ApiException("Số lượng sản phẩm không hợp lệ.", "INVALID_QTY");
            }
            List<LoHangService.PhanBoLo> phanBo = normalizePhanBoLos(item);
            String key;
            if (phanBo != null && phanBo.size() > 1) {
                key = item.getIdChiTietSanPham() + "|m|" + (multiSeq++);
            } else if (phanBo != null && phanBo.size() == 1) {
                key = item.getIdChiTietSanPham() + "|" + phanBo.get(0).idLoHang();
            } else {
                key = item.getIdChiTietSanPham() + "|_";
            }
            MergedLotLine existing = merged.get(key);
            if (existing == null) {
                Integer idLo = (phanBo != null && phanBo.size() == 1) ? phanBo.get(0).idLoHang() : null;
                List<LoHangService.PhanBoLo> multi =
                        (phanBo != null && phanBo.size() > 1) ? phanBo : null;
                merged.put(key, new MergedLotLine(
                        item.getIdChiTietSanPham(), idLo, item.getSoLuong(), multi));
            } else if (existing.phanBoLos != null) {
                throw new ApiException("Không gộp được dòng chọn nhiều lô.", "VALIDATION_ERROR");
            } else {
                existing.soLuong += item.getSoLuong();
            }
        }
        if (checkStock) {
            Map<Integer, Integer> totalByVariant = new HashMap<>();
            for (MergedLotLine m : merged.values()) {
                totalByVariant.merge(m.idChiTietSanPham, m.soLuong, Integer::sum);
            }
            for (Map.Entry<Integer, Integer> e : totalByVariant.entrySet()) {
                ChiTietSanPham cts = chiTietSanPhamRepository.findById(e.getKey())
                        .orElseThrow(() -> new ApiException(
                                "Biến thể sản phẩm không tồn tại (id=" + e.getKey() + ").", "NOT_FOUND"));
                int ton = cts.getSoLuongTon() != null ? cts.getSoLuongTon() : 0;
                if (ton < e.getValue()) {
                    throw new ApiException(
                            "Không đủ tồn cho SKU " + cts.getSku() + " (còn " + ton + ").",
                            "OUT_OF_STOCK");
                }
            }
        }
        List<LineCalc> lines = new ArrayList<>();
        for (MergedLotLine m : merged.values()) {
            lines.add(buildOneLine(
                    m.idChiTietSanPham, m.soLuong, m.idLoHang, m.phanBoLos, false, saleMap));
        }
        return lines;
    }

    /**
     * Chuẩn hóa phân bổ lô từ request.
     * @return null = FEFO; list size 1 = 1 lô; size &gt; 1 = nhiều lô
     */
    private List<LoHangService.PhanBoLo> normalizePhanBoLos(TaoDonTaiQuayRequest.ItemRequest item) {
        if (item.getPhanBoLos() != null && !item.getPhanBoLos().isEmpty()) {
            Map<Integer, Integer> qtyByLot = new LinkedHashMap<>();
            for (TaoDonTaiQuayRequest.PhanBoLoItem pb : item.getPhanBoLos()) {
                if (pb == null || pb.getIdLoHang() == null) {
                    throw new ApiException("Thiếu id lô trong phân bổ.", "VALIDATION_ERROR");
                }
                if (pb.getSoLuong() == null || pb.getSoLuong() <= 0) {
                    throw new ApiException("Số lượng lấy từ mỗi lô phải lớn hơn 0.", "VALIDATION_ERROR");
                }
                qtyByLot.merge(pb.getIdLoHang(), pb.getSoLuong(), Integer::sum);
            }
            int tong = qtyByLot.values().stream().mapToInt(Integer::intValue).sum();
            if (tong != item.getSoLuong()) {
                throw new ApiException(
                        "Tổng số lượng chọn không khớp (đã chọn " + tong
                                + ", cần bán " + item.getSoLuong() + ").",
                        "VALIDATION_ERROR");
            }
            List<LoHangService.PhanBoLo> list = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : qtyByLot.entrySet()) {
                list.add(new LoHangService.PhanBoLo(e.getKey(), e.getValue()));
            }
            return list;
        }
        if (item.getIdLoHang() != null) {
            return List.of(new LoHangService.PhanBoLo(item.getIdLoHang(), item.getSoLuong()));
        }
        return null;
    }

    private LineCalc buildOneLine(
            Integer idCts,
            int soLuong,
            Integer idLoHang,
            List<LoHangService.PhanBoLo> phanBoLos,
            boolean checkStock,
            Map<Integer, VariantSaleInfo> saleMap) {
        ChiTietSanPham cts = chiTietSanPhamRepository.findById(idCts)
                .orElseThrow(() -> new ApiException(
                        "Biến thể sản phẩm không tồn tại (id=" + idCts + ").", "NOT_FOUND"));
        if (!Boolean.TRUE.equals(cts.getTrangThai())) {
            throw new ApiException("SKU " + cts.getSku() + " không còn bán.", "INACTIVE_SKU");
        }
        if (checkStock) {
            int ton = cts.getSoLuongTon() != null ? cts.getSoLuongTon() : 0;
            if (ton < soLuong) {
                throw new ApiException(
                        "Không đủ tồn cho SKU " + cts.getSku() + " (còn " + ton + ").",
                        "OUT_OF_STOCK");
            }
        }
        BigDecimal donGia = checkoutPricingService.resolveDonGia(cts, saleMap);
        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Giá bán SKU " + cts.getSku() + " không hợp lệ.", "INVALID_PRICE");
        }
        BigDecimal thanhTienDong = donGia.multiply(BigDecimal.valueOf(soLuong));
        Integer singleLot = idLoHang;
        List<LoHangService.PhanBoLo> multi = phanBoLos;
        if (multi != null && multi.size() == 1) {
            singleLot = multi.get(0).idLoHang();
            multi = null;
        }
        return new LineCalc(cts, soLuong, donGia, thanhTienDong, singleLot, multi);
    }

    private LineCalc buildOneLine(
            Integer idCts,
            int soLuong,
            Integer idLoHang,
            boolean checkStock,
            Map<Integer, VariantSaleInfo> saleMap) {
        return buildOneLine(idCts, soLuong, idLoHang, null, checkStock, saleMap);
    }

    private BigDecimal sumTongTien(List<LineCalc> lines) {
        BigDecimal tong = BigDecimal.ZERO;
        for (LineCalc line : lines) {
            tong = tong.add(line.thanhTien);
        }
        return tong;
    }

    private void saveChiTietLines(HoaDon hoaDon, List<LineCalc> lines) {
        for (LineCalc line : lines) {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdChiTietSanPham(line.cts);
            hdct.setSoLuong(line.soLuong);
            hdct.setDonGia(line.donGia);
            hdct.setThanhTien(line.thanhTien);
            hoaDonChiTietRepository.save(hdct);

            loHangService.truTonVaGhiNhan(hdct, line.soLuong, line.idLoHang, line.phanBoLos);
        }
    }

    private void hoanTonTheoHoaDon(HoaDon hoaDon) {
        for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
            loHangService.hoanTonTheoChiTiet(chiTiet);
        }
    }

    private Map<Integer, Integer> mergeItems(List<TaoDonTaiQuayRequest.ItemRequest> items) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (TaoDonTaiQuayRequest.ItemRequest item : items) {
            if (item.getIdChiTietSanPham() == null || item.getSoLuong() == null || item.getSoLuong() <= 0) {
                throw new ApiException("Số lượng sản phẩm không hợp lệ.", "INVALID_QTY");
            }
            map.merge(item.getIdChiTietSanPham(), item.getSoLuong(), Integer::sum);
        }
        return map;
    }

    private String sinhMaHoaDon(LocalDateTime now) {
        String prefix = "HD" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 50; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String ma = prefix + suffix;
            if (!hoaDonRepository.existsByMaHoaDon(ma)) {
                return ma;
            }
        }
        throw new ApiException("Không thể sinh mã hóa đơn. Vui lòng thử lại.", "CODE_GEN_FAILED");
    }

    private void ghiNhatKy(HoaDon hoaDon, String maHanhDong, String ghiChu, NhanVien nhanVien,
                           LocalDateTime thoiGian) {
        LichSuDonHang ls = new LichSuDonHang();
        ls.setIdHoaDon(hoaDon);
        ls.setTrangThai(maHanhDong);
        ls.setGhiChu(ghiChu);
        ls.setIdNhanVien(nhanVien);
        ls.setThoiGian(thoiGian);
        lichSuDonHangRepository.save(ls);
    }

    private String tenSanPhamLine(ChiTietSanPham cts) {
        if (cts.getSanPham() != null && cts.getSanPham().getTen() != null) {
            return cts.getSanPham().getTen();
        }
        return cts.getSku();
    }

    private String bienTheLine(ChiTietSanPham cts) {
        String dt = cts.getDungTichMl() != null
                ? cts.getDungTichMl().stripTrailingZeros().toPlainString() + "ml" : null;
        String ms = cts.getMauSac() != null ? cts.getMauSac().getTen() : null;
        if (dt != null && ms != null) {
            return dt + " / " + ms;
        }
        if (dt != null) {
            return dt;
        }
        return ms != null ? ms : "";
    }

    private record LineCalc(
            ChiTietSanPham cts,
            int soLuong,
            BigDecimal donGia,
            BigDecimal thanhTien,
            Integer idLoHang,
            List<LoHangService.PhanBoLo> phanBoLos) {}

    private static final class MergedLotLine {
        final Integer idChiTietSanPham;
        final Integer idLoHang;
        final List<LoHangService.PhanBoLo> phanBoLos;
        int soLuong;

        MergedLotLine(
                Integer idChiTietSanPham,
                Integer idLoHang,
                int soLuong,
                List<LoHangService.PhanBoLo> phanBoLos) {
            this.idChiTietSanPham = idChiTietSanPham;
            this.idLoHang = idLoHang;
            this.soLuong = soLuong;
            this.phanBoLos = phanBoLos;
        }
    }
}
