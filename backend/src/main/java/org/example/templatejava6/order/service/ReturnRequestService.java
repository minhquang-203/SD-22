package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.enums.LoaiHangTra;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.service.ProductFileStorageService;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.OrderMailService;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.order.entity.AnhYeuCauTraHang;
import org.example.templatejava6.order.entity.ChiTietTraHangLo;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.HoaDonChiTietLo;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.model.request.NhanHangTraRequest;
import org.example.templatejava6.order.model.request.TaoYeuCauTraHangRequest;
import org.example.templatejava6.order.model.response.LoHangDonHangResponse;
import org.example.templatejava6.order.model.response.YeuCauTraHangResponse;
import org.example.templatejava6.order.repository.AnhYeuCauTraHangRepository;
import org.example.templatejava6.order.repository.ChiTietTraHangLoRepository;
import org.example.templatejava6.order.repository.HoaDonChiTietLoRepository;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.order.repository.YeuCauTraHangRepository;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.LoHang;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.realtime.service.OrderRealtimeService;
import org.example.templatejava6.shipping.model.request.CreateShippingOrderRequest;
import org.example.templatejava6.shipping.model.request.ReturnShippingOrderRequest;
import org.example.templatejava6.shipping.model.response.CreateShippingOrderResponse;
import org.example.templatejava6.shipping.model.response.GhnPickShiftResponse;
import org.example.templatejava6.shipping.service.ShippingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quan ly luong tra hang sau khi khach da nhan hang. Luong giong nhau cho moi phuong thuc thanh toan:
 * <ol>
 *   <li>Khach gui yeu cau -> {@code CHO_DUYET}.</li>
 *   <li>Admin duyet -> {@code DA_DUYET} va bao khach de khach tao van don hoan. Khong hoan tien o buoc nay.</li>
 *   <li>Khach tao van don GHN hoan hang kem ca lay hang (pick_shift) -> {@code DANG_HOAN_HANG}.</li>
 *   <li>Van don hoan hoan thanh (GHN {@code delivered}) — cap nhat trang thai GHN;
 *       nhan vien bam "Da nhan hang" kem phan loai lo TOT/LOI -> {@code DA_NHAN_HANG}:
 *       TOT hoan ve dung lo, LOI tang so_luong_loi; tao ban ghi hoan tien {@code CHO_XU_LY}.</li>
 *   <li>Admin quyet dinh hoan tien hay tu choi tai trang hoan tien -> {@code HOAN_TAT}
 *       (xem {@link RefundService}).</li>
 * </ol>
 */
@Service
public class ReturnRequestService {

    private static final String LOAI_DON_ONLINE = "ONLINE";
    private static final String MA_VNPAY = "VNPAY";
    private static final int MIN_RETURN_IMAGES = 2;

    /** Trang thai GHN cho biet kien hang hoan da ve tay shop. */
    private static final List<String> GHN_TRANG_THAI_DA_VE_SHOP = List.of("delivered", "returned");

    private final YeuCauTraHangRepository yeuCauTraHangRepository;
    private final AnhYeuCauTraHangRepository anhYeuCauTraHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonChiTietLoRepository hoaDonChiTietLoRepository;
    private final ChiTietTraHangLoRepository chiTietTraHangLoRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangService loHangService;
    private final ShippingService shippingService;
    private final GhnTrackingService ghnTrackingService;
    private final RefundService refundService;
    private final ThongBaoService thongBaoService;
    private final OrderMailService orderMailService;
    private final ProductFileStorageService productFileStorageService;
    private final OrderRealtimeService orderRealtimeService;

    public ReturnRequestService(YeuCauTraHangRepository yeuCauTraHangRepository,
                                AnhYeuCauTraHangRepository anhYeuCauTraHangRepository,
                                HoaDonRepository hoaDonRepository,
                                HoaDonChiTietRepository hoaDonChiTietRepository,
                                HoaDonChiTietLoRepository hoaDonChiTietLoRepository,
                                ChiTietTraHangLoRepository chiTietTraHangLoRepository,
                                LichSuDonHangRepository lichSuDonHangRepository,
                                NhanVienRepository nhanVienRepository,
                                LoHangService loHangService,
                                ShippingService shippingService,
                                GhnTrackingService ghnTrackingService,
                                RefundService refundService,
                                ThongBaoService thongBaoService,
                                OrderMailService orderMailService,
                                ProductFileStorageService productFileStorageService,
                                OrderRealtimeService orderRealtimeService) {
        this.yeuCauTraHangRepository = yeuCauTraHangRepository;
        this.anhYeuCauTraHangRepository = anhYeuCauTraHangRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.hoaDonChiTietLoRepository = hoaDonChiTietLoRepository;
        this.chiTietTraHangLoRepository = chiTietTraHangLoRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.loHangService = loHangService;
        this.shippingService = shippingService;
        this.ghnTrackingService = ghnTrackingService;
        this.refundService = refundService;
        this.thongBaoService = thongBaoService;
        this.orderMailService = orderMailService;
        this.productFileStorageService = productFileStorageService;
        this.orderRealtimeService = orderRealtimeService;
    }

    /** Khach hang gui yeu cau tra hang cho mot don da nhan (HOAN_THANH). */
    @Transactional
    public YeuCauTraHangResponse taoYeuCau(Integer idKhachHang, Integer idHoaDon,
                                           TaoYeuCauTraHangRequest request,
                                           List<MultipartFile> files) {
        HoaDon hoaDon = loadOwnedOnlineOrder(idKhachHang, idHoaDon);
        if (hoaDon.getTrangThai() != TrangThaiDonHang.HOAN_THANH) {
            throw new ApiException(
                    "Chỉ có thể yêu cầu trả hàng cho đơn đã giao thành công.", "ORDER_NOT_DELIVERED");
        }
        if (yeuCauTraHangRepository.existsByIdHoaDon_IdAndTrangThaiNotIn(
                idHoaDon, List.of(TrangThaiTraHang.TU_CHOI))) {
            throw new ApiException("Đơn hàng đã có yêu cầu trả hàng đang xử lý.", "RETURN_ALREADY_EXISTS");
        }
        if (!laVnpay(hoaDon) && (request.getSoTaiKhoan() == null || request.getSoTaiKhoan().isBlank())) {
            throw new ApiException(
                    "Vui lòng cung cấp thông tin tài khoản ngân hàng để nhận tiền hoàn.",
                    "BANK_INFO_REQUIRED");
        }

        List<MultipartFile> validFiles = filterValidFiles(files);
        if (validFiles.size() < MIN_RETURN_IMAGES) {
            throw new ApiException(
                    "Vui lòng tải lên tối thiểu " + MIN_RETURN_IMAGES + " hình ảnh sản phẩm.",
                    "RETURN_IMAGES_REQUIRED");
        }

        YeuCauTraHang yc = new YeuCauTraHang();
        yc.setIdHoaDon(hoaDon);
        yc.setLyDo(request.getLyDo());
        yc.setMoTa(request.getMoTa());
        yc.setTrangThai(TrangThaiTraHang.CHO_DUYET);
        yc.setGhnDistrictId(request.getGhnDistrictId() != null
                ? request.getGhnDistrictId() : hoaDon.getGhnDistrictId());
        yc.setGhnWardCode(coGiaTri(request.getGhnWardCode())
                ? request.getGhnWardCode().trim() : hoaDon.getGhnWardCode());
        yc.setDiaChiTra(coGiaTri(request.getDiaChiTra())
                ? request.getDiaChiTra().trim() : hoaDon.getDiaChiGiao());
        yc.setTenNganHang(request.getTenNganHang());
        yc.setSoTaiKhoan(request.getSoTaiKhoan());
        yc.setChuTaiKhoan(request.getChuTaiKhoan());
        yc.setNgayTao(LocalDateTime.now());
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);

        List<String> anhUrls = luuAnhTraHang(saved, validFiles);

        ghiNhatKy(hoaDon, "YEU_CAU_TRA_HANG", "Khách gửi yêu cầu trả hàng: "
                + (request.getLyDo() != null ? request.getLyDo() : ""));
        thongBaoService.taoThongBao(
                LoaiThongBao.YEU_CAU_TRA_HANG,
                "Yêu cầu trả hàng",
                "Đơn " + hoaDon.getMaHoaDon() + " có yêu cầu trả hàng mới.",
                "/admin/tra-hang",
                saved.getId(),
                hoaDon.getMaHoaDon());
        return new YeuCauTraHangResponse(saved, anhUrls);
    }

    @Transactional(readOnly = true)
    public List<YeuCauTraHangResponse> danhSachCuaToi(Integer idKhachHang) {
        return yeuCauTraHangRepository
                .findByIdHoaDon_IdKhachHang_IdOrderByNgayTaoDesc(idKhachHang)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<YeuCauTraHangResponse> danhSach(TrangThaiTraHang trangThai) {
        List<YeuCauTraHang> list = trangThai != null
                ? yeuCauTraHangRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai)
                : yeuCauTraHangRepository.findAllByOrderByNgayTaoDesc();
        return list.stream().map(this::toResponse).toList();
    }

    /**
     * Admin duyet yeu cau tra hang: danh dau {@code DA_DUYET}, chuyen hoa don sang {@code TRA_HANG}
     * de khach thay trang thai tren storefront, roi bao khach tao van don hoan.
     * Khong hoan tien o buoc nay — hoan tien chi duoc xem xet sau khi shop nhan lai hang.
     */
    @Transactional
    public YeuCauTraHangResponse duyet(Integer id, Integer idNhanVien, String ghiChu) {
        YeuCauTraHang yc = load(id);
        if (yc.getTrangThai() != TrangThaiTraHang.CHO_DUYET) {
            throw new ApiException("Yêu cầu trả hàng không ở trạng thái chờ duyệt.", "INVALID_RETURN_STATUS");
        }
        HoaDon hoaDon = yc.getIdHoaDon();
        yc.setIdNhanVienDuyet(resolveNhanVien(idNhanVien));
        yc.setGhiChuAdmin(ghiChu);
        yc.setTrangThai(TrangThaiTraHang.DA_DUYET);
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);

        TrangThaiDonHang trangThaiCu = hoaDon.getTrangThai();
        if (trangThaiCu != TrangThaiDonHang.TRA_HANG) {
            hoaDon.setTrangThai(TrangThaiDonHang.TRA_HANG);
            hoaDonRepository.save(hoaDon);
            orderRealtimeService.publishStatusChanged(hoaDon, trangThaiCu);
        }

        ghiNhatKy(hoaDon, "TRA_HANG_DA_DUYET", "Duyệt yêu cầu trả hàng — đơn chuyển TRA_HANG, chờ khách tạo vận đơn hoàn hàng");
        orderMailService.guiYeuCauTraHangDuocDuyet(hoaDon);
        return toResponse(saved);
    }

    /** Admin tu choi yeu cau tra hang. */
    @Transactional
    public YeuCauTraHangResponse tuChoi(Integer id, String lyDo, Integer idNhanVien) {
        YeuCauTraHang yc = load(id);
        if (yc.getTrangThai() != TrangThaiTraHang.CHO_DUYET) {
            throw new ApiException("Yêu cầu trả hàng không ở trạng thái chờ duyệt.", "INVALID_RETURN_STATUS");
        }
        yc.setTrangThai(TrangThaiTraHang.TU_CHOI);
        yc.setIdNhanVienDuyet(resolveNhanVien(idNhanVien));
        yc.setGhiChuAdmin(lyDo);
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);
        ghiNhatKy(yc.getIdHoaDon(), "TRA_HANG_TU_CHOI",
                "Từ chối yêu cầu trả hàng" + (lyDo != null && !lyDo.isBlank() ? ": " + lyDo : ""));
        orderMailService.guiYeuCauTraHangBiTuChoi(yc.getIdHoaDon(), lyDo);
        return toResponse(saved);
    }

    /** Danh sach ca lay hang GHN de khach chon thoi diem shipper den lay hang tra. */
    @Transactional(readOnly = true)
    public List<GhnPickShiftResponse> danhSachCaLayHang() {
        return shippingService.getPickShifts();
    }

    /**
     * Khach tao van don GHN hoan tra hang ve shop (sau khi da duoc duyet),
     * kem ca lay hang GHN de chon thoi diem shipper den lay.
     */
    @Transactional
    public YeuCauTraHangResponse taoVanDonTra(Integer idKhachHang, Integer idYeuCau, Integer pickShiftId) {
        YeuCauTraHang yc = load(idYeuCau);
        HoaDon hoaDon = yc.getIdHoaDon();
        if (hoaDon.getIdKhachHang() == null || !hoaDon.getIdKhachHang().getId().equals(idKhachHang)) {
            throw new ApiException("Không tìm thấy yêu cầu trả hàng.", "NOT_FOUND");
        }
        if (yc.getTrangThai() != TrangThaiTraHang.DA_DUYET) {
            throw new ApiException(
                    "Yêu cầu trả hàng chưa được duyệt hoặc đã tạo vận đơn.", "INVALID_RETURN_STATUS");
        }
        if (yc.getGhnDistrictId() == null || yc.getGhnWardCode() == null || yc.getGhnWardCode().isBlank()) {
            throw new ApiException(
                    "Thiếu địa chỉ (quận/huyện, phường/xã) để lấy hàng trả.", "GHN_MISSING_ADDRESS");
        }
        GhnPickShiftResponse caLayHang = resolveCaLayHang(pickShiftId);

        ReturnShippingOrderRequest req = new ReturnShippingOrderRequest();
        req.setFromName(orElse(hoaDon.getTenNguoiNhan(),
                hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getHoTen() : "Khách hàng"));
        req.setFromPhone(orElse(hoaDon.getSdtNguoiNhan(),
                hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getSoDienThoai() : null));
        req.setFromAddress(orElse(yc.getDiaChiTra(), hoaDon.getDiaChiGiao()));
        req.setFromDistrictId(yc.getGhnDistrictId());
        req.setFromWardCode(yc.getGhnWardCode());
        req.setItems(buildItems(hoaDon));
        if (caLayHang != null) {
            req.setPickShiftId(caLayHang.getId());
        }

        CreateShippingOrderResponse response = shippingService.createReturnOrder(req);
        yc.setMaVanDonTra(response.getOrderCode());
        yc.setTrangThai(TrangThaiTraHang.DANG_HOAN_HANG);
        if (caLayHang != null) {
            yc.setPickShiftId(caLayHang.getId());
            yc.setPickShiftLabel(caLayHang.getTitle());
        }
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);
        ghiNhatKy(hoaDon, "TRA_HANG_DANG_HOAN", "Đã tạo vận đơn hoàn trả GHN: " + response.getOrderCode()
                + (caLayHang != null ? " — " + caLayHang.getTitle() : ""));
        return toResponse(saved);
    }

    /**
     * Dong bo trang thai van don hoan tu GHN. Khi kien hang da ve shop (GHN {@code delivered})
     * thi tu dong ghi nhan da nhan hang de mo buoc quyet dinh hoan tien.
     */
    @Transactional
    public YeuCauTraHangResponse dongBoVanDonTra(Integer id, Integer idNhanVien) {
        YeuCauTraHang yc = load(id);
        String maVanDon = yc.getMaVanDonTra();
        if (maVanDon == null || maVanDon.isBlank()) {
            throw new ApiException(
                    "Yêu cầu trả hàng chưa có vận đơn hoàn để đồng bộ.", "RETURN_NO_TRACKING");
        }
        String trangThaiGhn = ghnTrackingService.track(maVanDon)
                .map(GhnTrackingService.TrackingInfo::status)
                .orElseThrow(() -> new ApiException(
                        "Không lấy được trạng thái vận đơn hoàn từ GHN.", "GHN_ERROR"));

        yc.setGhnTrangThaiTra(trangThaiGhn);
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);

        // Không tự hoàn kho — nhân viên phải xác nhận nhận hàng kèm phân loại TỐT/LỖI theo lô.
        boolean daVeShop = GHN_TRANG_THAI_DA_VE_SHOP.contains(trangThaiGhn);
        boolean choNhanHang = saved.getTrangThai() == TrangThaiTraHang.DANG_HOAN_HANG;
        if (daVeShop && choNhanHang) {
            ghiNhatKy(saved.getIdHoaDon(), "TRA_HANG_GHN_DA_VE",
                    "Vận đơn hoàn GHN " + maVanDon + " đã về shop ("
                            + GhnTrackingService.labelOf(trangThaiGhn)
                            + ") — chờ nhân viên xác nhận nhận hàng và phân loại lô TỐT/LỖI");
        }
        return toResponse(saved);
    }

    /**
     * Danh sách lô mà đơn đã lấy (từ hoa_don_chi_tiet_lo) — để nhân viên chọn khi nhận hàng trả.
     */
    @Transactional(readOnly = true)
    public List<LoHangDonHangResponse> danhSachLoCuaYeuCau(Integer idYeuCau) {
        YeuCauTraHang yc = load(idYeuCau);
        return danhSachLoCuaHoaDon(yc.getIdHoaDon());
    }

    @Transactional(readOnly = true)
    public List<LoHangDonHangResponse> danhSachLoCuaHoaDon(HoaDon hoaDon) {
        List<HoaDonChiTietLo> rows = hoaDonChiTietLoRepository.findByHoaDonFetchLo(hoaDon);
        Map<Integer, LoHangDonHangResponse> byLot = new LinkedHashMap<>();
        for (HoaDonChiTietLo row : rows) {
            LoHang lo = row.getLoHang();
            if (lo == null || lo.getId() == null) {
                continue;
            }
            LoHangDonHangResponse item = byLot.get(lo.getId());
            if (item == null) {
                item = new LoHangDonHangResponse();
                item.setIdLoHang(lo.getId());
                item.setSoLo(lo.getSoLo());
                item.setHanSuDung(lo.getHanSuDung());
                item.setNgayNhap(lo.getNgayNhap());
                item.setSoLuongDaBan(0);
                HoaDonChiTiet ct = row.getHoaDonChiTiet();
                ChiTietSanPham cts = ct != null ? ct.getIdChiTietSanPham() : null;
                if (cts != null) {
                    item.setIdChiTietSanPham(cts.getId());
                    item.setSku(cts.getSku());
                    SanPham sp = cts.getSanPham();
                    item.setTenSanPham(sp != null ? sp.getTen() : null);
                }
                byLot.put(lo.getId(), item);
            }
            int add = row.getSoLuong() != null ? row.getSoLuong() : 0;
            item.setSoLuongDaBan(item.getSoLuongDaBan() + add);
        }
        return new ArrayList<>(byLot.values());
    }

    /** Admin xac nhan thu cong da nhan lai hang tra (kem phan bo lo TOT/LOI). */
    @Transactional
    public YeuCauTraHangResponse xacNhanNhanHang(Integer id, Integer idNhanVien,
                                                 List<NhanHangTraRequest.ChiTietLoRequest> chiTietLo) {
        YeuCauTraHang yc = load(id);
        if (yc.getTrangThai() != TrangThaiTraHang.DANG_HOAN_HANG) {
            throw new ApiException(
                    "Chỉ xác nhận nhận hàng khi khách đã tạo vận đơn hoàn (đang hoàn hàng).",
                    "INVALID_RETURN_STATUS");
        }
        if (yc.getMaVanDonTra() == null || yc.getMaVanDonTra().isBlank()) {
            throw new ApiException(
                    "Khách chưa tạo vận đơn hoàn hàng. Không thể xác nhận đã nhận hàng.",
                    "RETURN_NO_TRACKING");
        }
        return toResponse(ghiNhanDaNhanHang(yc, idNhanVien,
                "Admin xác nhận đã nhận lại hàng trả", chiTietLo));
    }

    /**
     * Ghi nhan shop da nhan lai hang: hoan ton theo lo TOT/LOI, don chuyen {@code TRA_HANG},
     * yeu cau tra hang sang {@code DA_NHAN_HANG} va tao ban ghi hoan tien {@code CHO_XU_LY}
     * de admin quyet dinh hoan tien hay tu choi.
     */
    private YeuCauTraHang ghiNhanDaNhanHang(YeuCauTraHang yc, Integer idNhanVien, String ghiChu,
                                            List<NhanHangTraRequest.ChiTietLoRequest> chiTietLo) {
        HoaDon hoaDon = yc.getIdHoaDon();
        TrangThaiDonHang trangThaiCu = hoaDon.getTrangThai();

        hoanTonTheoPhanBoTra(yc, hoaDon, chiTietLo);
        if (trangThaiCu != TrangThaiDonHang.TRA_HANG) {
            hoaDon.setTrangThai(TrangThaiDonHang.TRA_HANG);
            hoaDonRepository.save(hoaDon);
            orderRealtimeService.publishStatusChanged(hoaDon, trangThaiCu);
        }

        LocalDateTime now = LocalDateTime.now();
        yc.setTrangThai(TrangThaiTraHang.DA_NHAN_HANG);
        yc.setNgayNhanHang(now);
        yc.setNgayCapNhat(now);
        NhanVien nhanVien = resolveNhanVien(idNhanVien);
        if (nhanVien != null) {
            yc.setIdNhanVienDuyet(nhanVien);
        }
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);

        ghiNhatKy(hoaDon, "TRA_HANG_DA_NHAN_HANG", ghiChu + " — hoàn tồn theo lô, chờ quyết định hoàn tiền");
        refundService.taoHoanTienTraHangNeuChua(
                hoaDon, refundService.resolveSoTienHoan(hoaDon), saved,
                yc.getTenNganHang(), yc.getSoTaiKhoan(), yc.getChuTaiKhoan());
        orderMailService.guiDaNhanHangTra(hoaDon);
        return saved;
    }

    /**
     * Hoàn tồn theo phân bổ TỐT/LỖI nhân viên chọn.
     * Đơn cũ không có hoa_don_chi_tiet_lo: fallback hoàn tất cả như hàng tốt.
     */
    private void hoanTonTheoPhanBoTra(YeuCauTraHang yc, HoaDon hoaDon,
                                      List<NhanHangTraRequest.ChiTietLoRequest> chiTietLo) {
        List<HoaDonChiTietLo> soldLots = hoaDonChiTietLoRepository.findByHoaDonFetchLo(hoaDon);
        if (soldLots.isEmpty()) {
            for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
                loHangService.hoanTonTheoChiTiet(chiTiet);
            }
            return;
        }

        if (chiTietLo == null || chiTietLo.isEmpty()) {
            throw new ApiException(
                    "Vui lòng phân bổ số lượng trả về từng lô (TỐT/LỖI).",
                    "RETURN_LOT_REQUIRED");
        }

        Map<Integer, Integer> purchasedByLot = new HashMap<>();
        Set<Integer> validLotIds = new HashSet<>();
        for (HoaDonChiTietLo row : soldLots) {
            Integer idLo = row.getLoHang() != null ? row.getLoHang().getId() : null;
            if (idLo == null) {
                continue;
            }
            validLotIds.add(idLo);
            int qty = row.getSoLuong() != null ? row.getSoLuong() : 0;
            purchasedByLot.merge(idLo, qty, Integer::sum);
        }

        Map<Integer, Integer> returnedByLot = new HashMap<>();
        for (NhanHangTraRequest.ChiTietLoRequest item : chiTietLo) {
            if (item == null || item.getIdLoHang() == null) {
                throw new ApiException("Thiếu id lô hàng trong phân bổ trả.", "VALIDATION_ERROR");
            }
            if (item.getSoLuong() == null || item.getSoLuong() <= 0) {
                throw new ApiException("Số lượng trả phải > 0.", "VALIDATION_ERROR");
            }
            if (item.getLoaiHang() == null) {
                throw new ApiException("Thiếu loại hàng trả (TOT/LOI).", "VALIDATION_ERROR");
            }
            if (!validLotIds.contains(item.getIdLoHang())) {
                throw new ApiException(
                        "Lô #" + item.getIdLoHang() + " không thuộc đơn đang trả.",
                        "RETURN_LOT_INVALID");
            }
            returnedByLot.merge(item.getIdLoHang(), item.getSoLuong(), Integer::sum);
        }

        for (Map.Entry<Integer, Integer> e : returnedByLot.entrySet()) {
            int purchased = purchasedByLot.getOrDefault(e.getKey(), 0);
            if (e.getValue() > purchased) {
                throw new ApiException(
                        "Số lượng trả lô #" + e.getKey() + " (" + e.getValue()
                                + ") vượt số đã bán từ lô (" + purchased + ").",
                        "RETURN_QTY_EXCEEDED");
            }
        }
        for (Map.Entry<Integer, Integer> e : purchasedByLot.entrySet()) {
            int returned = returnedByLot.getOrDefault(e.getKey(), 0);
            if (returned != e.getValue()) {
                throw new ApiException(
                        "Phải phân bổ đủ số lượng đã bán của mỗi lô. Lô #" + e.getKey()
                                + ": đã bán " + e.getValue() + ", đang phân bổ " + returned + ".",
                        "RETURN_QTY_MISMATCH");
            }
        }

        for (NhanHangTraRequest.ChiTietLoRequest item : chiTietLo) {
            if (item.getLoaiHang() == LoaiHangTra.TOT) {
                loHangService.hoanTonVaoLo(item.getIdLoHang(), item.getSoLuong());
            } else {
                loHangService.ghiNhanHangLoi(item.getIdLoHang(), item.getSoLuong());
            }

            ChiTietTraHangLo detail = new ChiTietTraHangLo();
            detail.setYeuCauTraHang(yc);
            LoHang loRef = loHangService.getLoHangRef(item.getIdLoHang());
            detail.setLoHang(loRef);
            detail.setSoLuong(item.getSoLuong());
            detail.setLoai(item.getLoaiHang());
            chiTietTraHangLoRepository.save(detail);
        }

        Set<Integer> clearedHdct = new HashSet<>();
        for (HoaDonChiTietLo row : soldLots) {
            HoaDonChiTiet ct = row.getHoaDonChiTiet();
            if (ct == null || ct.getId() == null || !clearedHdct.add(ct.getId())) {
                continue;
            }
            hoaDonChiTietLoRepository.deleteByHoaDonChiTiet(ct);
        }
    }

    /** Kiem tra ca lay hang khach chon con nam trong danh sach GHN dang mo. */
    private GhnPickShiftResponse resolveCaLayHang(Integer pickShiftId) {
        if (pickShiftId == null) {
            return null;
        }
        return shippingService.getPickShifts().stream()
                .filter(shift -> pickShiftId.equals(shift.getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        "Ca lấy hàng không còn hiệu lực, vui lòng chọn lại.", "GHN_INVALID_PICK_SHIFT"));
    }

    private YeuCauTraHangResponse toResponse(YeuCauTraHang yc) {
        List<String> anhUrls = anhYeuCauTraHangRepository
                .findByIdYeuCauTraHang_IdOrderByIdAsc(yc.getId())
                .stream()
                .map(AnhYeuCauTraHang::getDuongDan)
                .toList();
        return new YeuCauTraHangResponse(yc, anhUrls);
    }

    private List<MultipartFile> filterValidFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        List<MultipartFile> valid = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                valid.add(file);
            }
        }
        return valid;
    }

    private List<String> luuAnhTraHang(YeuCauTraHang yeuCau, List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (MultipartFile file : files) {
            String path = productFileStorageService.store(file);
            AnhYeuCauTraHang anh = new AnhYeuCauTraHang();
            anh.setIdYeuCauTraHang(yeuCau);
            anh.setDuongDan(path);
            anh.setNgayTao(now);
            anhYeuCauTraHangRepository.save(anh);
            urls.add(path);
        }
        return urls;
    }

    private boolean laVnpay(HoaDon hoaDon) {
        return hoaDon != null
                && hoaDon.getIdPhuongThucThanhToan() != null
                && MA_VNPAY.equalsIgnoreCase(hoaDon.getIdPhuongThucThanhToan().getMa());
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
        return "Sản phẩm";
    }

    private HoaDon loadOwnedOnlineOrder(Integer idKhachHang, Integer idHoaDon) {
        if (idKhachHang == null) {
            throw new ApiException("Thiếu thông tin khách hàng.", "VALIDATION_ERROR");
        }
        return hoaDonRepository.findByIdAndIdKhachHang_IdAndLoaiDon(idHoaDon, idKhachHang, LOAI_DON_ONLINE)
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng online.", "NOT_FOUND"));
    }

    private YeuCauTraHang load(Integer id) {
        return yeuCauTraHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu trả hàng.", "NOT_FOUND"));
    }

    private NhanVien resolveNhanVien(Integer idNhanVien) {
        if (idNhanVien == null) {
            return null;
        }
        return nhanVienRepository.findById(idNhanVien).orElse(null);
    }

    private void ghiNhatKy(HoaDon hoaDon, String trangThai, String ghiChu) {
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setIdHoaDon(hoaDon);
        lichSu.setTrangThai(trangThai);
        lichSu.setGhiChu(ghiChu != null && ghiChu.length() > 255 ? ghiChu.substring(0, 255) : ghiChu);
        lichSu.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(lichSu);
    }

    private static String orElse(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private boolean coGiaTri(String value) {
        return value != null && !value.isBlank();
    }
}
