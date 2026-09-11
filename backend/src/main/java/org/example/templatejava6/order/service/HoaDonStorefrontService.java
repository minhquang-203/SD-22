package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.HoanTien;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.model.response.StorefrontOrderDetailResponse;
import org.example.templatejava6.order.model.response.StorefrontOrderLineResponse;
import org.example.templatejava6.order.model.response.StorefrontOrderSummaryResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.HoanTienRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.YeuCauTraHangRepository;
import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.repository.AnhSanPhamRepository;
import org.example.templatejava6.review.repository.DanhGiaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonStorefrontService {

    /** Số ngày cho phép yêu cầu trả hàng kể từ lúc đơn chuyển sang HOAN_THANH. */
    private static final int SO_NGAY_CHO_PHEP_TRA_HANG = 7;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final AnhSanPhamRepository anhSanPhamRepository;
    private final GhnTrackingService ghnTrackingService;
    private final DanhGiaRepository danhGiaRepository;
    private final OnlineOrderLifecycleService onlineOrderLifecycleService;
    private final YeuCauTraHangRepository yeuCauTraHangRepository;
    private final HoanTienRepository hoanTienRepository;
    private final TransactionTemplate readOnlyTx;

    public HoaDonStorefrontService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            KhachHangRepository khachHangRepository,
            AnhSanPhamRepository anhSanPhamRepository,
            GhnTrackingService ghnTrackingService,
            DanhGiaRepository danhGiaRepository,
            OnlineOrderLifecycleService onlineOrderLifecycleService,
            YeuCauTraHangRepository yeuCauTraHangRepository,
            HoanTienRepository hoanTienRepository,
            PlatformTransactionManager transactionManager) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.anhSanPhamRepository = anhSanPhamRepository;
        this.ghnTrackingService = ghnTrackingService;
        this.danhGiaRepository = danhGiaRepository;
        this.onlineOrderLifecycleService = onlineOrderLifecycleService;
        this.yeuCauTraHangRepository = yeuCauTraHangRepository;
        this.hoanTienRepository = hoanTienRepository;
        this.readOnlyTx = new TransactionTemplate(transactionManager);
        this.readOnlyTx.setReadOnly(true);
    }

    @Transactional(readOnly = true)
    public List<StorefrontOrderSummaryResponse> donCuaToi() {
        KhachHang kh = getKhachDangNhap();
        return hoaDonRepository.findByIdKhachHang_IdOrderByNgayTaoDesc(kh.getId())
                .stream()
                // VNPAY chưa thanh toán: coi như chưa đặt — không hiện ở tra cứu / đơn hàng.
                .filter(hd -> !onlineOrderLifecycleService.laVnpayChuaThanhToan(hd))
                .map(this::buildSummary)
                .toList();
    }

    /**
     * Chi tiết đơn phía khách: đọc DB trong transaction ngắn, rồi mới gọi GHN (không giữ connection).
     */
    public StorefrontOrderDetailResponse chiTietCuaToi(Integer id) {
        StorefrontOrderDetailResponse detail = readOnlyTx.execute(status -> loadChiTietCuaToi(id));
        enrichLiveGhnTracking(detail);
        return detail;
    }

    private StorefrontOrderDetailResponse loadChiTietCuaToi(Integer id) {
        KhachHang kh = getKhachDangNhap();
        HoaDon hd = hoaDonRepository.findByIdAndIdKhachHang_Id(id, kh.getId())
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND"));
        if (onlineOrderLifecycleService.laVnpayChuaThanhToan(hd)) {
            throw new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND");
        }
        return buildDetail(hd);
    }

    @Transactional
    public StorefrontOrderDetailResponse huyDonCuaToi(Integer id, String ghiChu) {
        KhachHang kh = getKhachDangNhap();
        HoaDon hd = hoaDonRepository.findByIdAndIdKhachHang_Id(id, kh.getId())
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND"));
        onlineOrderLifecycleService.huyDonOnline(
                hd,
                ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Khách hàng hủy đơn online");
        HoaDon updated = hoaDonRepository.findById(id).orElse(hd);
        StorefrontOrderDetailResponse detail = buildDetail(updated);
        enrichLiveGhnTracking(detail);
        return detail;
    }

    private StorefrontOrderSummaryResponse buildSummary(HoaDon hd) {
        StorefrontOrderSummaryResponse r = new StorefrontOrderSummaryResponse();
        r.setId(hd.getId());
        r.setMaHoaDon(hd.getMaHoaDon());
        r.setNgayTao(hd.getNgayTao());
        r.setTrangThai(hd.getTrangThai() != null ? hd.getTrangThai().name() : null);
        r.setTrangThaiLabel(mapStatusLabel(hd.getTrangThai()));
        r.setThanhTien(hd.getThanhTien());
        applyShippingFromDb(r::setMaVanDon, r::setDonViVanChuyen, null, hd);
        if (hd.getIdPhuongThucThanhToan() != null) {
            r.setMaPhuongThucThanhToan(hd.getIdPhuongThucThanhToan().getMa());
        }

        List<HoaDonChiTiet> lines = hoaDonChiTietRepository.findByIdHoaDon(hd);
        r.setSoDongHang(lines.size());
        if (!lines.isEmpty()) {
            HoaDonChiTiet first = lines.get(0);
            r.setSoLuong(first.getSoLuong());
            ChiTietSanPham cts = first.getIdChiTietSanPham();
            if (cts != null) {
                SanPham sp = cts.getSanPham();
                if (sp != null) {
                    r.setTenSanPham(sp.getTen());
                    r.setAnhUrl(resolveAnhUrl(sp.getId()));
                }
            }
        }

        applyTraHangSummary(r, hd);
        return r;
    }

    /** Gắn thông tin trả hàng nhẹ vào summary để tab/filter & click điều hướng không cần N× chi tiết. */
    private void applyTraHangSummary(StorefrontOrderSummaryResponse r, HoaDon hd) {
        List<YeuCauTraHang> yeuCaus = yeuCauTraHangRepository.findByIdHoaDonOrderByNgayTaoDesc(hd);
        if (yeuCaus.isEmpty()) {
            return;
        }
        YeuCauTraHang moiNhat = yeuCaus.get(0);
        r.setIdYeuCauTraHang(moiNhat.getId());
        if (moiNhat.getTrangThai() != null) {
            r.setTrangThaiTraHang(moiNhat.getTrangThai().name());
            r.setTrangThaiTraHangLabel(moiNhat.getTrangThai().getLabelChoKhach());
        }
        r.setMaVanDonTra(moiNhat.getMaVanDonTra());
    }

    private StorefrontOrderDetailResponse buildDetail(HoaDon hd) {
        StorefrontOrderDetailResponse r = new StorefrontOrderDetailResponse();
        r.setId(hd.getId());
        r.setMaHoaDon(hd.getMaHoaDon());
        r.setNgayTao(hd.getNgayTao());
        r.setTrangThai(hd.getTrangThai() != null ? hd.getTrangThai().name() : null);
        r.setTrangThaiLabel(mapStatusLabel(hd.getTrangThai()));
        r.setTongTien(defaultZero(hd.getTongTien()));
        r.setTienGiamGia(defaultZero(hd.getTienGiamGia()));
        r.setPhiVanChuyen(defaultZero(hd.getPhiVanChuyen()));
        applyShippingFromDb(r::setMaVanDon, r::setDonViVanChuyen, r::setGhnTrangThaiLabel, hd);
        r.setThanhTien(defaultZero(hd.getThanhTien()));
        r.setTenNguoiNhan(resolveTenNguoiNhan(hd));
        r.setSdtNguoiNhan(resolveSdtNguoiNhan(hd));
        r.setDiaChiGiao(hd.getDiaChiGiao());
        if (hd.getIdPhuongThucThanhToan() != null) {
            r.setMaPhuongThucThanhToan(hd.getIdPhuongThucThanhToan().getMa());
        }
        r.setChiTiets(hoaDonChiTietRepository.findByIdHoaDon(hd).stream()
                .map(this::buildLine)
                .toList());

        applyCapNhatGanNhat(r, hd);
        applyTraHangVaHoanTien(r, hd);
        return r;
    }

    /**
     * "Cập nhật gần nhất" luôn theo trạng thái hiện tại của đơn (không lấy nhầm bản ghi lịch sử cũ
     * khi nhiều dòng cùng thoi_gian — ví dụ seed data hoặc ghi nhật ký hàng loạt).
     */
    private void applyCapNhatGanNhat(StorefrontOrderDetailResponse r, HoaDon hd) {
        List<LichSuDonHang> lichSu = lichSuDonHangRepository
                .findByIdHoaDon_IdOrderByThoiGianDescIdDesc(hd.getId());
        TrangThaiDonHang current = hd.getTrangThai();
        if (current != null) {
            r.setCapNhatGanNhatTrangThai(current.name());
            r.setCapNhatGanNhatLabel(mapStatusLabel(current));
            r.setCapNhatGanNhatLuc(resolveThoiGianCapNhat(lichSu, current, hd));
            if (current == TrangThaiDonHang.DA_HUY) {
                applyThongTinHuy(r, lichSu);
            }
            return;
        }
        if (!lichSu.isEmpty()) {
            LichSuDonHang latest = lichSu.get(0);
            r.setCapNhatGanNhatTrangThai(latest.getTrangThai());
            r.setCapNhatGanNhatLabel(resolveLichSuLabel(latest.getTrangThai()));
            r.setCapNhatGanNhatLuc(latest.getThoiGian() != null ? latest.getThoiGian() : hd.getNgayTao());
        } else {
            r.setCapNhatGanNhatLuc(hd.getNgayTao());
        }
    }

    private void applyThongTinHuy(StorefrontOrderDetailResponse r, List<LichSuDonHang> lichSu) {
        for (LichSuDonHang ls : lichSu) {
            if (!TrangThaiDonHang.DA_HUY.name().equals(ls.getTrangThai())) {
                continue;
            }
            String ghiChu = ls.getGhiChu();
            r.setLyDoHuy(ghiChu);
            r.setHuyBoiCuaHang(OnlineOrderLifecycleService.laHuyBoiCuaHang(ghiChu));
            return;
        }
        r.setHuyBoiCuaHang(true);
        r.setLyDoHuy("Cửa hàng hủy đơn hàng");
    }

    private static LocalDateTime resolveThoiGianCapNhat(
            List<LichSuDonHang> lichSu,
            TrangThaiDonHang current,
            HoaDon hd) {
        // Ưu tiên thời điểm ghi nhận đúng trạng thái hiện tại.
        for (LichSuDonHang ls : lichSu) {
            if (current.name().equals(ls.getTrangThai()) && ls.getThoiGian() != null) {
                return ls.getThoiGian();
            }
        }
        // Không có dòng khớp: lấy lần cập nhật trạng thái đơn gần nhất (bỏ sự kiện phụ).
        for (LichSuDonHang ls : lichSu) {
            if (isMaTrangThaiDonHang(ls.getTrangThai()) && ls.getThoiGian() != null) {
                return ls.getThoiGian();
            }
        }
        if (!lichSu.isEmpty() && lichSu.get(0).getThoiGian() != null) {
            return lichSu.get(0).getThoiGian();
        }
        return hd.getNgayTao();
    }

    private static boolean isMaTrangThaiDonHang(String ma) {
        if (ma == null || ma.isBlank()) {
            return false;
        }
        try {
            TrangThaiDonHang.valueOf(ma);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void applyTraHangVaHoanTien(StorefrontOrderDetailResponse r, HoaDon hd) {
        List<YeuCauTraHang> yeuCaus = yeuCauTraHangRepository.findByIdHoaDonOrderByNgayTaoDesc(hd);
        YeuCauTraHang moiNhat = yeuCaus.isEmpty() ? null : yeuCaus.get(0);
        // Chỉ đơn đã giao, chưa từng gửi yêu cầu, và còn trong hạn 7 ngày.
        r.setCoTheYeuCauTraHang(coTheYeuCauTraHang(r, hd, yeuCaus));
        if (moiNhat != null) {
            r.setIdYeuCauTraHang(moiNhat.getId());
            if (moiNhat.getTrangThai() != null) {
                r.setTrangThaiTraHang(moiNhat.getTrangThai().name());
                r.setTrangThaiTraHangLabel(moiNhat.getTrangThai().getLabelChoKhach());
            }
            if (moiNhat.getTrangThai() == TrangThaiTraHang.TU_CHOI) {
                r.setLyDoTuChoiTraHang(moiNhat.getGhiChuAdmin());
            }
            r.setMaVanDonTra(moiNhat.getMaVanDonTra());
            r.setPickShiftLabel(moiNhat.getPickShiftLabel());
            r.setGhnTrangThaiTraLabel(GhnTrackingService.labelOf(moiNhat.getGhnTrangThaiTra()));
        }

        List<HoanTien> hoanTiens = hoanTienRepository.findByIdHoaDonOrderByNgayTaoDesc(hd);
        if (!hoanTiens.isEmpty()) {
            HoanTien htMoiNhat = hoanTiens.get(0);
            if (htMoiNhat.getTrangThai() != null) {
                r.setTrangThaiHoanTien(htMoiNhat.getTrangThai().name());
                r.setTrangThaiHoanTienLabel(htMoiNhat.getTrangThai().getLabel());
            }
            r.setMaGiaoDichHoan(htMoiNhat.getMaGiaoDichHoan());
        }
    }

    private boolean coTheYeuCauTraHang(
            StorefrontOrderDetailResponse r,
            HoaDon hd,
            List<YeuCauTraHang> yeuCaus) {
        if (hd.getTrangThai() != TrangThaiDonHang.HOAN_THANH || !yeuCaus.isEmpty()) {
            return false;
        }
        LocalDateTime ngayGiao = r.getCapNhatGanNhatLuc() != null
                ? r.getCapNhatGanNhatLuc()
                : hd.getNgayTao();
        if (ngayGiao == null) {
            return false;
        }
        return !LocalDateTime.now().isAfter(ngayGiao.plusDays(SO_NGAY_CHO_PHEP_TRA_HANG));
    }

    private void applyShippingFromDb(
            java.util.function.Consumer<String> setMaVanDon,
            java.util.function.Consumer<String> setDonViVanChuyen,
            java.util.function.Consumer<String> setStatusLabelFallback,
            HoaDon hd) {
        String maVanDon = normalizeMa(hd.getMaVanDonGhn());
        if (maVanDon.isBlank()) {
            return;
        }
        setMaVanDon.accept(maVanDon);
        setDonViVanChuyen.accept("Giao hàng nhanh");
        if (setStatusLabelFallback != null) {
            String fallback = fallbackGhnStatusLabel(hd.getTrangThai());
            if (fallback != null) {
                setStatusLabelFallback.accept(fallback);
            }
        }
    }

    /**
     * Gọi GHN sau khi đã đóng transaction DB — chỉ để làm giàu trạng thái/ETA.
     * Mã vận đơn và đơn vị VC luôn lấy từ DB ở {@link #applyShippingFromDb}.
     */
    private void enrichLiveGhnTracking(StorefrontOrderDetailResponse r) {
        if (r == null) {
            return;
        }
        // Đơn hủy: không gọi GHN, không hiện dự kiến giao.
        if ("DA_HUY".equals(r.getTrangThai())) {
            r.setGhnHenGiao(null);
            if (r.getGhnTrangThaiLabel() == null || r.getGhnTrangThaiLabel().isBlank()) {
                r.setGhnTrangThaiLabel("Đã hủy");
            }
            return;
        }
        String maVanDon = normalizeMa(r.getMaVanDon());
        if (maVanDon.isBlank()) {
            return;
        }
        try {
            ghnTrackingService.track(maVanDon).ifPresent(info -> {
                r.setGhnTrangThai(info.status());
                r.setGhnTrangThaiLabel(info.statusLabel());
                r.setGhnHenGiao(info.leadtime());
            });
        } catch (Exception ignored) {
            // Giữ thông tin vận chuyển từ DB.
        }
    }

    private static String fallbackGhnStatusLabel(TrangThaiDonHang trangThai) {
        if (trangThai == null) {
            return null;
        }
        return switch (trangThai) {
            case DANG_CHUAN_BI -> "Đang chuẩn bị giao";
            case DANG_GIAO -> "Đang vận chuyển";
            case HOAN_THANH -> "Đã giao";
            case DA_HUY -> "Đã hủy";
            default -> null;
        };
    }

    private StorefrontOrderLineResponse buildLine(HoaDonChiTiet ct) {
        StorefrontOrderLineResponse line = new StorefrontOrderLineResponse();
        line.setId(ct.getId());
        ChiTietSanPham cts = ct.getIdChiTietSanPham();
        if (cts != null) {
            line.setIdChiTietSanPham(cts.getId());
            SanPham sp = cts.getSanPham();
            line.setTenSanPham(sp != null ? sp.getTen() : null);
            if (sp != null) {
                line.setIdSanPham(sp.getId());
                line.setAnhUrl(resolveAnhUrl(sp.getId()));
            }
            line.setBienThe(buildBienThe(cts));
        }
        line.setSoLuong(ct.getSoLuong());
        line.setDonGia(ct.getDonGia());
        line.setThanhTien(ct.getThanhTien());
        applyReviewStatus(line, ct.getId());
        return line;
    }

    private void applyReviewStatus(StorefrontOrderLineResponse line, Integer idHoaDonChiTiet) {
        danhGiaRepository.findFirstByHoaDonChiTiet_Id(idHoaDonChiTiet).ifPresentOrElse(dg -> {
            line.setDaDanhGia(true);
            line.setTrangThaiDanhGia(dg.getTrangThai());
        }, () -> {
            line.setDaDanhGia(false);
            line.setTrangThaiDanhGia(null);
        });
    }

    private String buildBienThe(ChiTietSanPham cts) {
        String dt = cts.getDungTichMl() != null
                ? cts.getDungTichMl().stripTrailingZeros().toPlainString() + "ml"
                : null;
        String ms = cts.getMauSac() != null ? cts.getMauSac().getTen() : null;
        if (dt != null && ms != null) {
            return dt + " · " + ms;
        }
        if (dt != null) {
            return dt;
        }
        return ms;
    }

    private String resolveAnhUrl(Integer idSanPham) {
        if (idSanPham == null) {
            return null;
        }
        Optional<AnhSanPham> anh = anhSanPhamRepository.findFirstBySanPham_IdAndLaAnhChinhTrue(idSanPham);
        if (anh.isEmpty()) {
            anh = anhSanPhamRepository.findFirstBySanPham_IdOrderByThuTuAsc(idSanPham);
        }
        return anh.map(AnhSanPham::getUrl).orElse(null);
    }

    private String resolveTenNguoiNhan(HoaDon hd) {
        if (hd.getTenNguoiNhan() != null && !hd.getTenNguoiNhan().isBlank()) {
            return hd.getTenNguoiNhan();
        }
        return hd.getIdKhachHang() != null ? hd.getIdKhachHang().getHoTen() : null;
    }

    private String resolveSdtNguoiNhan(HoaDon hd) {
        if (hd.getSdtNguoiNhan() != null && !hd.getSdtNguoiNhan().isBlank()) {
            return hd.getSdtNguoiNhan();
        }
        return hd.getIdKhachHang() != null ? hd.getIdKhachHang().getSoDienThoai() : null;
    }

    private KhachHang getKhachDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ApiException("Chưa đăng nhập", "UNAUTHORIZED");
        }
        Integer id;
        try {
            id = Integer.parseInt(auth.getName());
        } catch (NumberFormatException ex) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy tài khoản", "NOT_FOUND"));
    }

    static String normalizeMa(String ma) {
        return ma == null ? "" : ma.trim();
    }

    static BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    static String mapStatusLabel(TrangThaiDonHang trangThai) {
        if (trangThai == null) {
            return "—";
        }
        return switch (trangThai) {
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DA_XAC_NHAN -> "Đã xác nhận";
            case DANG_CHUAN_BI -> "Đang chuẩn bị hàng";
            case DANG_GIAO -> "Đang giao";
            case HOAN_THANH -> "Đã giao";
            case TRA_HANG -> "Trả hàng";
            case DA_HUY -> "Đã hủy";
            default -> trangThai.getLabel();
        };
    }

    private static String resolveLichSuLabel(String ma) {
        if (ma == null) {
            return null;
        }
        try {
            return mapStatusLabel(TrangThaiDonHang.valueOf(ma));
        } catch (IllegalArgumentException ignored) {
            return switch (ma) {
                case "TAO_DON" -> "Tạo đơn";
                case "DA_XAC_NHAN" -> "Đã xác nhận";
                case "THANH_TOAN" -> "Thanh toán";
                case "TRU_TON" -> "Giữ hàng";
                default -> ma;
            };
        }
    }
}
