package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.model.response.StorefrontOrderDetailResponse;
import org.example.templatejava6.order.model.response.StorefrontOrderLineResponse;
import org.example.templatejava6.order.model.response.StorefrontOrderSummaryResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.example.templatejava6.product.entity.AnhSanPham;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.repository.AnhSanPhamRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonStorefrontService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final AnhSanPhamRepository anhSanPhamRepository;

    public HoaDonStorefrontService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            LichSuDonHangRepository lichSuDonHangRepository,
            KhachHangRepository khachHangRepository,
            AnhSanPhamRepository anhSanPhamRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.anhSanPhamRepository = anhSanPhamRepository;
    }

    @Transactional(readOnly = true)
    public Optional<StorefrontOrderDetailResponse> traCuu(String ma, String sdt) {
        String maNorm = normalizeMa(ma);
        String sdtNorm = normalizePhone(sdt);
        if (maNorm.isBlank() || sdtNorm.isBlank()) {
            return Optional.empty();
        }
        return hoaDonRepository.findByMaHoaDonIgnoreCase(maNorm)
                .filter(hd -> phoneMatches(hd, sdtNorm))
                .map(this::buildDetail);
    }

    @Transactional(readOnly = true)
    public List<StorefrontOrderSummaryResponse> donCuaToi() {
        KhachHang kh = getKhachDangNhap();
        return hoaDonRepository.findByIdKhachHang_IdOrderByNgayTaoDesc(kh.getId())
                .stream()
                .map(this::buildSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public StorefrontOrderDetailResponse chiTietCuaToi(Integer id) {
        KhachHang kh = getKhachDangNhap();
        HoaDon hd = hoaDonRepository.findByIdAndIdKhachHang_Id(id, kh.getId())
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND"));
        return buildDetail(hd);
    }

    private StorefrontOrderSummaryResponse buildSummary(HoaDon hd) {
        StorefrontOrderSummaryResponse r = new StorefrontOrderSummaryResponse();
        r.setId(hd.getId());
        r.setMaHoaDon(hd.getMaHoaDon());
        r.setNgayTao(hd.getNgayTao());
        r.setTrangThai(hd.getTrangThai() != null ? hd.getTrangThai().name() : null);
        r.setTrangThaiLabel(mapStatusLabel(hd.getTrangThai()));
        r.setThanhTien(hd.getThanhTien());
        return r;
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
        r.setThanhTien(defaultZero(hd.getThanhTien()));
        r.setTenNguoiNhan(resolveTenNguoiNhan(hd));
        r.setSdtNguoiNhan(resolveSdtNguoiNhan(hd));
        r.setDiaChiGiao(hd.getDiaChiGiao());
        r.setChiTiets(hoaDonChiTietRepository.findByIdHoaDon(hd).stream()
                .map(this::buildLine)
                .toList());

        List<LichSuDonHang> lichSu = lichSuDonHangRepository
                .findByIdHoaDon_IdOrderByThoiGianDesc(hd.getId());
        if (!lichSu.isEmpty()) {
            LichSuDonHang latest = lichSu.get(0);
            r.setCapNhatGanNhatTrangThai(latest.getTrangThai());
            r.setCapNhatGanNhatLabel(resolveLichSuLabel(latest.getTrangThai()));
            r.setCapNhatGanNhatLuc(latest.getThoiGian());
        }
        return r;
    }

    private StorefrontOrderLineResponse buildLine(HoaDonChiTiet ct) {
        StorefrontOrderLineResponse line = new StorefrontOrderLineResponse();
        ChiTietSanPham cts = ct.getIdChiTietSanPham();
        if (cts != null) {
            SanPham sp = cts.getSanPham();
            line.setTenSanPham(sp != null ? sp.getTen() : null);
            line.setBienThe(buildBienThe(cts));
            if (sp != null) {
                line.setAnhUrl(resolveAnhUrl(sp.getId()));
            }
        }
        line.setSoLuong(ct.getSoLuong());
        line.setDonGia(ct.getDonGia());
        line.setThanhTien(ct.getThanhTien());
        return line;
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

    private boolean phoneMatches(HoaDon hd, String sdtNorm) {
        String sdtNhan = normalizePhone(hd.getSdtNguoiNhan());
        if (!sdtNhan.isBlank() && sdtNhan.equals(sdtNorm)) {
            return true;
        }
        if (hd.getIdKhachHang() != null) {
            return normalizePhone(hd.getIdKhachHang().getSoDienThoai()).equals(sdtNorm);
        }
        return false;
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

    static String normalizePhone(String sdt) {
        if (sdt == null) {
            return "";
        }
        return sdt.replaceAll("[\\s\\-.]", "");
    }

    static BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    static String mapStatusLabel(TrangThaiDonHang trangThai) {
        if (trangThai == null) {
            return "—";
        }
        return switch (trangThai) {
            case CHO_XAC_NHAN, DA_XAC_NHAN, DANG_CHUAN_BI -> "Chờ xác nhận";
            case DANG_GIAO -> "Đang giao";
            case HOAN_THANH -> "Đã giao";
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
