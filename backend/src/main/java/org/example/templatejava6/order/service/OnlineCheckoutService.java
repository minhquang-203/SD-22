package org.example.templatejava6.order.service;

import org.example.templatejava6.cart.entity.ChiTietGioHang;
import org.example.templatejava6.cart.entity.GioHang;
import org.example.templatejava6.cart.repository.ChiTietGioHangRepository;
import org.example.templatejava6.cart.repository.GioHangRepository;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.entity.PhuongThucThanhToan;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.ThanhToanHoaDon;
import org.example.templatejava6.order.model.request.HuyDonOnlineRequest;
import org.example.templatejava6.order.model.request.OnlineCheckoutRequest;
import org.example.templatejava6.order.model.response.HoaDonChiTietResponse;
import org.example.templatejava6.order.model.response.HoaDonDetailResponse;
import org.example.templatejava6.order.model.response.HoaDonResponse;
import org.example.templatejava6.order.model.response.OnlineCheckoutResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.PhuongThucThanhToanRepository;
import org.example.templatejava6.order.repository.ThanhToanHoaDonRepository;
import org.example.templatejava6.payment.model.request.TaoThanhToanRequest;
import org.example.templatejava6.payment.model.response.TaoThanhToanResponse;
import org.example.templatejava6.payment.service.PaymentService;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.service.LoHangService;
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
            OnlineOrderLifecycleService onlineOrderLifecycleService) {
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
    }

    @Transactional
    public OnlineCheckoutResponse checkout(OnlineCheckoutRequest request, String clientIp) {
        KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                .orElseThrow(() -> new ApiException("Khách hàng không tồn tại.", "NOT_FOUND"));
        String maPhuongThuc = normalize(request.getMaPhuongThucThanhToan());
        if (!MA_COD.equals(maPhuongThuc) && !MA_VNPAY.equals(maPhuongThuc)) {
            throw new ApiException("Chỉ hỗ trợ COD hoặc VNPAY cho bán hàng online.", "UNSUPPORTED_PAYMENT_METHOD");
        }
        PhuongThucThanhToan phuongThuc = resolvePhuongThuc(maPhuongThuc);

        GioHang gioHang = gioHangRepository.findFirstByKhachHang_IdOrderByIdAsc(khachHang.getId())
                .orElseThrow(() -> new ApiException("Khách hàng chưa có giỏ hàng.", "EMPTY_CART"));
        List<ChiTietGioHang> selectedItems = resolveSelectedCartItems(gioHang, request.getIdsChiTietGioHang());
        List<LineCalc> lines = buildLines(selectedItems);
        BigDecimal tongTien = sumTongTien(lines);

        PhieuGiamGia phieu = resolvePhieu(request.getMaPhieuGiamGia());
        BigDecimal tienGiamGia = phieu != null ? tinhTienGiam(phieu, tongTien) : BigDecimal.ZERO;
        BigDecimal phiVanChuyen = request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO;
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
        hoaDon.setTrangThai(MA_COD.equals(maPhuongThuc) ? TrangThaiDonHang.DA_XAC_NHAN : TrangThaiDonHang.CHO_XAC_NHAN);
        hoaDon.setDiaChiGiao(request.getDiaChiGiao().trim());
        hoaDon.setTenNguoiNhan(khachHang.getHoTen());
        hoaDon.setSdtNguoiNhan(khachHang.getSoDienThoai());
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
            loHangService.truTonTheoFefo(line.chiTietSanPham().getId(), line.soLuong());
        }

        if (phieu != null) {
            phieu.setSoLuong(phieu.getSoLuong() - 1);
            phieuGiamGiaRepository.save(phieu);
        }
        chiTietGioHangRepository.deleteAll(selectedItems);
        ghiNhatKy(hoaDon, "TAO_DON", "Khách hàng tạo đơn online từ " + selectedItems.size() + " sản phẩm đã chọn");
        ghiNhatKy(hoaDon, "TRU_TON", "Đã giữ hàng cho đơn online");

        TaoThanhToanResponse payment = null;
        if (MA_COD.equals(maPhuongThuc)) {
            taoThanhToanCod(hoaDon, phuongThuc, now);
            ghiNhatKy(hoaDon, "DA_XAC_NHAN", "Đơn COD đã được ghi nhận, chờ giao hàng");
        } else {
            TaoThanhToanRequest paymentRequest = new TaoThanhToanRequest();
            paymentRequest.setIdHoaDon(hoaDon.getId());
            payment = paymentService.taoThanhToan(MA_VNPAY, paymentRequest, clientIp);
        }
        return OnlineCheckoutResponse.from(hoaDon, payment);
    }

    @Transactional(readOnly = true)
    public List<HoaDonResponse> danhSachDonHang(Integer idKhachHang) {
        validateKhachHang(idKhachHang);
        return hoaDonRepository.findByIdKhachHang_IdAndLoaiDonOrderByNgayTaoDesc(idKhachHang, LOAI_DON_ONLINE)
                .stream()
                .map(HoaDonResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public HoaDonDetailResponse chiTietDonHang(Integer idKhachHang, Integer idHoaDon) {
        validateKhachHang(idKhachHang);
        HoaDon hoaDon = loadOwnedOnlineOrder(idKhachHang, idHoaDon);
        return buildDetailResponse(hoaDon);
    }

    @Transactional
    public HoaDonDetailResponse huyDonHang(Integer idKhachHang, Integer idHoaDon, HuyDonOnlineRequest request) {
        validateKhachHang(idKhachHang);
        HoaDon hoaDon = loadOwnedOnlineOrder(idKhachHang, idHoaDon);
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

    private List<LineCalc> buildLines(List<ChiTietGioHang> cartItems) {
        if (cartItems.isEmpty()) {
            throw new ApiException("Vui lòng chọn ít nhất một sản phẩm để thanh toán.", "EMPTY_SELECTION");
        }
        Map<Integer, LineCalc> lines = new LinkedHashMap<>();
        for (ChiTietGioHang item : cartItems) {
            ChiTietSanPham chiTietSanPham = item.getChiTietSanPham();
            validateChiTietSanPham(chiTietSanPham, item.getSoLuong());
            int soLuong = item.getSoLuong();
            BigDecimal donGia = chiTietSanPham.getGiaBan();
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

    private BigDecimal tinhTienGiam(PhieuGiamGia phieu, BigDecimal tongTien) {
        if (!Boolean.TRUE.equals(phieu.getTrangThai())) {
            throw new ApiException("Mã giảm giá không còn hiệu lực.", "INVALID_VOUCHER");
        }
        LocalDateTime now = LocalDateTime.now();
        if (phieu.getNgayBatDau() != null && now.isBefore(phieu.getNgayBatDau())) {
            throw new ApiException("Mã giảm giá chưa đến thời gian áp dụng.", "INVALID_VOUCHER");
        }
        if (phieu.getNgayKetThuc() != null && now.isAfter(phieu.getNgayKetThuc())) {
            throw new ApiException("Mã giảm giá đã hết hạn.", "INVALID_VOUCHER");
        }
        if (phieu.getSoLuong() == null || phieu.getSoLuong() <= 0) {
            throw new ApiException("Mã giảm giá đã hết lượt sử dụng.", "INVALID_VOUCHER");
        }
        BigDecimal donToiThieu = phieu.getGiaTriDonToiThieu() != null
                ? phieu.getGiaTriDonToiThieu()
                : BigDecimal.ZERO;
        if (tongTien.compareTo(donToiThieu) < 0) {
            throw new ApiException(
                    "Đơn hàng chưa đạt giá trị tối thiểu " + donToiThieu.toPlainString() + "đ.",
                    "INVALID_VOUCHER");
        }

        BigDecimal giam;
        if (phieu.getLoai() == LoaiPhieuGiamGia.PHAN_TRAM) {
            giam = tongTien.multiply(phieu.getGiaTri())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (phieu.getGiamToiDa() != null && giam.compareTo(phieu.getGiamToiDa()) > 0) {
                giam = phieu.getGiamToiDa();
            }
        } else if (phieu.getLoai() == LoaiPhieuGiamGia.TIEN_MAT) {
            giam = phieu.getGiaTri();
        } else {
            throw new ApiException("Loại mã giảm giá không hợp lệ.", "INVALID_VOUCHER");
        }
        return giam.compareTo(tongTien) > 0 ? tongTien : giam;
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

    private void validateKhachHang(Integer idKhachHang) {
        if (idKhachHang == null || !khachHangRepository.existsById(idKhachHang)) {
            throw new ApiException("Khách hàng không tồn tại.", "NOT_FOUND");
        }
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
