package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.enums.LoaiHoanTien;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.enums.TrangThaiTraHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.OrderMailService;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.model.request.TaoYeuCauTraHangRequest;
import org.example.templatejava6.order.model.response.YeuCauTraHangResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.order.repository.YeuCauTraHangRepository;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.service.LoHangService;
import org.example.templatejava6.shipping.model.request.CreateShippingOrderRequest;
import org.example.templatejava6.shipping.model.request.ReturnShippingOrderRequest;
import org.example.templatejava6.shipping.model.response.CreateShippingOrderResponse;
import org.example.templatejava6.shipping.service.ShippingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quan ly luong tra hang sau khi khach da nhan hang:
 * - Chuyen khoan: khach gui yeu cau -> admin duyet -> khach tao van don hoan
 *   -> admin xac nhan nhan hang -> TRA_HANG + yeu cau hoan tien thu cong.
 * - VNPAY: khach gui yeu cau -> admin duyet -> hoan kho + goi refund API ngay
 *   -> hoa don TRA_HANG, hoan tien DA_HOAN (co ma giao dich refund).
 */
@Service
public class ReturnRequestService {

    private static final String LOAI_DON_ONLINE = "ONLINE";
    private static final String MA_VNPAY = "VNPAY";

    private final YeuCauTraHangRepository yeuCauTraHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangService loHangService;
    private final ShippingService shippingService;
    private final RefundService refundService;
    private final ThongBaoService thongBaoService;
    private final OrderMailService orderMailService;

    public ReturnRequestService(YeuCauTraHangRepository yeuCauTraHangRepository,
                                HoaDonRepository hoaDonRepository,
                                HoaDonChiTietRepository hoaDonChiTietRepository,
                                LichSuDonHangRepository lichSuDonHangRepository,
                                NhanVienRepository nhanVienRepository,
                                LoHangService loHangService,
                                ShippingService shippingService,
                                RefundService refundService,
                                ThongBaoService thongBaoService,
                                OrderMailService orderMailService) {
        this.yeuCauTraHangRepository = yeuCauTraHangRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.loHangService = loHangService;
        this.shippingService = shippingService;
        this.refundService = refundService;
        this.thongBaoService = thongBaoService;
        this.orderMailService = orderMailService;
    }

    /** Khach hang gui yeu cau tra hang cho mot don da nhan (HOAN_THANH). */
    @Transactional
    public YeuCauTraHangResponse taoYeuCau(Integer idKhachHang, Integer idHoaDon, TaoYeuCauTraHangRequest request) {
        HoaDon hoaDon = loadOwnedOnlineOrder(idKhachHang, idHoaDon);
        if (hoaDon.getTrangThai() != TrangThaiDonHang.HOAN_THANH) {
            throw new ApiException(
                    "Chỉ có thể yêu cầu trả hàng cho đơn đã giao thành công.", "ORDER_NOT_DELIVERED");
        }
        if (yeuCauTraHangRepository.existsByIdHoaDon_IdAndTrangThaiNotIn(
                idHoaDon, List.of(TrangThaiTraHang.TU_CHOI))) {
            throw new ApiException("Đơn hàng đã có yêu cầu trả hàng đang xử lý.", "RETURN_ALREADY_EXISTS");
        }
        boolean laVnpay = hoaDon.getIdPhuongThucThanhToan() != null
                && MA_VNPAY.equalsIgnoreCase(hoaDon.getIdPhuongThucThanhToan().getMa());
        if (!laVnpay && (request.getSoTaiKhoan() == null || request.getSoTaiKhoan().isBlank())) {
            throw new ApiException(
                    "Vui lòng cung cấp thông tin tài khoản ngân hàng để nhận tiền hoàn.",
                    "BANK_INFO_REQUIRED");
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

        ghiNhatKy(hoaDon, "YEU_CAU_TRA_HANG", "Khách gửi yêu cầu trả hàng: "
                + (request.getLyDo() != null ? request.getLyDo() : ""));
        thongBaoService.taoThongBao(
                LoaiThongBao.YEU_CAU_TRA_HANG,
                "Yêu cầu trả hàng",
                "Đơn " + hoaDon.getMaHoaDon() + " có yêu cầu trả hàng mới.",
                "/admin/tra-hang",
                saved.getId(),
                hoaDon.getMaHoaDon());
        return new YeuCauTraHangResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<YeuCauTraHangResponse> danhSachCuaToi(Integer idKhachHang) {
        return yeuCauTraHangRepository
                .findByIdHoaDon_IdKhachHang_IdOrderByNgayTaoDesc(idKhachHang)
                .stream().map(YeuCauTraHangResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<YeuCauTraHangResponse> danhSach(TrangThaiTraHang trangThai) {
        List<YeuCauTraHang> list = trangThai != null
                ? yeuCauTraHangRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai)
                : yeuCauTraHangRepository.findAllByOrderByNgayTaoDesc();
        return list.stream().map(YeuCauTraHangResponse::new).toList();
    }

    /**
     * Admin duyet yeu cau tra hang.
     * - VNPAY: hoan kho, goi refund API ngay → hoa don TRA_HANG, hoan tien DA_HOAN + ma GD.
     * - Khac (chuyen khoan): chi danh dau DA_DUYET, cho khach giao hang ve roi moi hoan tien.
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
        yc.setNgayCapNhat(LocalDateTime.now());

        if (laVnpay(hoaDon)) {
            hoanTonKho(hoaDon);
            yc.setTrangThai(TrangThaiTraHang.HOAN_TAT);
            YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);
            ghiNhatKy(hoaDon, "TRA_HANG_DA_DUYET", "Duyệt trả hàng VNPAY — hoàn kho và hoàn tiền tự động");
            refundService.taoVaHoanTuDong(
                    hoaDon,
                    LoaiHoanTien.TRA_HANG,
                    refundService.resolveSoTienHoan(hoaDon),
                    saved,
                    null, null, null,
                    idNhanVien);
            orderMailService.guiYeuCauTraHangDuocDuyet(hoaDon);
            return new YeuCauTraHangResponse(saved);
        }

        yc.setTrangThai(TrangThaiTraHang.DA_DUYET);
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);
        ghiNhatKy(hoaDon, "TRA_HANG_DA_DUYET", "Duyệt yêu cầu trả hàng");
        orderMailService.guiYeuCauTraHangDuocDuyet(hoaDon);
        return new YeuCauTraHangResponse(saved);
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
        return new YeuCauTraHangResponse(saved);
    }

    /** Khach tao van don GHN hoan tra hang ve shop (sau khi da duoc duyet). */
    @Transactional
    public YeuCauTraHangResponse taoVanDonTra(Integer idKhachHang, Integer idYeuCau) {
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

        ReturnShippingOrderRequest req = new ReturnShippingOrderRequest();
        req.setFromName(orElse(hoaDon.getTenNguoiNhan(),
                hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getHoTen() : "Khách hàng"));
        req.setFromPhone(orElse(hoaDon.getSdtNguoiNhan(),
                hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getSoDienThoai() : null));
        req.setFromAddress(orElse(yc.getDiaChiTra(), hoaDon.getDiaChiGiao()));
        req.setFromDistrictId(yc.getGhnDistrictId());
        req.setFromWardCode(yc.getGhnWardCode());
        req.setItems(buildItems(hoaDon));

        CreateShippingOrderResponse response = shippingService.createReturnOrder(req);
        yc.setMaVanDonTra(response.getOrderCode());
        yc.setTrangThai(TrangThaiTraHang.DANG_HOAN_HANG);
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);
        ghiNhatKy(hoaDon, "TRA_HANG_DANG_HOAN", "Đã tạo vận đơn hoàn trả GHN: " + response.getOrderCode());
        return new YeuCauTraHangResponse(saved);
    }

    /** Admin xac nhan da nhan lai hang: don chuyen TRA_HANG, hoan ton kho va tao yeu cau hoan tien. */
    @Transactional
    public YeuCauTraHangResponse xacNhanNhanHang(Integer id, Integer idNhanVien) {
        YeuCauTraHang yc = load(id);
        if (yc.getTrangThai() != TrangThaiTraHang.DANG_HOAN_HANG
                && yc.getTrangThai() != TrangThaiTraHang.DA_DUYET) {
            throw new ApiException(
                    "Yêu cầu trả hàng không ở trạng thái có thể xác nhận nhận hàng.", "INVALID_RETURN_STATUS");
        }
        HoaDon hoaDon = yc.getIdHoaDon();

        hoanTonKho(hoaDon);
        hoaDon.setTrangThai(TrangThaiDonHang.TRA_HANG);
        hoaDonRepository.save(hoaDon);

        yc.setTrangThai(TrangThaiTraHang.HOAN_TAT);
        yc.setIdNhanVienDuyet(resolveNhanVien(idNhanVien));
        yc.setNgayCapNhat(LocalDateTime.now());
        YeuCauTraHang saved = yeuCauTraHangRepository.save(yc);

        ghiNhatKy(hoaDon, "TRA_HANG", "Đã nhận lại hàng trả, hoàn tồn kho");
        if (laVnpay(hoaDon)) {
            // Luong cu: da duyet truoc do → xac nhan nhan hang thi goi refund VNPAY ngay.
            refundService.taoVaHoanTuDong(
                    hoaDon,
                    LoaiHoanTien.TRA_HANG,
                    refundService.resolveSoTienHoan(hoaDon),
                    saved,
                    null, null, null,
                    idNhanVien);
        } else {
            refundService.taoHoanTienChoXuLy(
                    hoaDon, LoaiHoanTien.TRA_HANG, hoaDon.getThanhTien(), saved,
                    yc.getTenNganHang(), yc.getSoTaiKhoan(), yc.getChuTaiKhoan());
        }
        return new YeuCauTraHangResponse(saved);
    }

    private boolean laVnpay(HoaDon hoaDon) {
        return hoaDon != null
                && hoaDon.getIdPhuongThucThanhToan() != null
                && MA_VNPAY.equalsIgnoreCase(hoaDon.getIdPhuongThucThanhToan().getMa());
    }

    private void hoanTonKho(HoaDon hoaDon) {
        for (HoaDonChiTiet chiTiet : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
            if (chiTiet.getIdChiTietSanPham() != null) {
                loHangService.hoanTon(chiTiet.getIdChiTietSanPham().getId(), chiTiet.getSoLuong());
            }
        }
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
