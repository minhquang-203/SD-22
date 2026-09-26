package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.service.ThongBaoService;
import org.example.templatejava6.order.entity.YeuCauMuaSoLuongLon;
import org.example.templatejava6.order.model.request.CapNhatYeuCauMuaSoLuongLonRequest;
import org.example.templatejava6.order.model.request.TaoYeuCauMuaSoLuongLonRequest;
import org.example.templatejava6.order.model.response.YeuCauMuaSoLuongLonResponse;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.order.repository.YeuCauMuaSoLuongLonRepository;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class YeuCauMuaSoLuongLonService {

    private static final Set<String> TRANG_THAI_HOP_LE =
            Set.of("MOI", "DA_LIEN_HE", "HOAN_TAT", "HUY");
    private static final int SPAM_WINDOW_MINUTES = 5;
    private static final int MIN_SO_LUONG = 16;

    private final YeuCauMuaSoLuongLonRepository repository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ThongBaoService thongBaoService;

    public YeuCauMuaSoLuongLonService(
            YeuCauMuaSoLuongLonRepository repository,
            ChiTietSanPhamRepository chiTietSanPhamRepository,
            KhachHangRepository khachHangRepository,
            NhanVienRepository nhanVienRepository,
            ThongBaoService thongBaoService) {
        this.repository = repository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.thongBaoService = thongBaoService;
    }

    @Transactional
    public YeuCauMuaSoLuongLonResponse tao(TaoYeuCauMuaSoLuongLonRequest req) {
        if (req.getSoLuong() == null || req.getSoLuong() < MIN_SO_LUONG) {
            throw new ApiException(
                    "Tối thiểu " + MIN_SO_LUONG + " sản phẩm cho đơn số lượng lớn",
                    "INVALID_QTY");
        }

        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdWithSanPham(req.getIdChiTietSanPham())
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));

        int ton = ct.getSoLuongTon() != null ? ct.getSoLuongTon() : 0;
        if (ton < MIN_SO_LUONG) {
            throw new ApiException(
                    "Sản phẩm không đủ tồn để đăng ký mua số lượng lớn (còn " + ton + " sản phẩm)",
                    "INSUFFICIENT_STOCK");
        }
        if (req.getSoLuong() > ton) {
            throw new ApiException(
                    "Số lượng yêu cầu vượt quá tồn kho hiện có (còn " + ton + " sản phẩm)",
                    "INVALID_QTY");
        }

        String hoTen = normalizeSpaces(req.getHoTen());
        if (hoTen == null || hoTen.length() < 2 || hoTen.length() > 60) {
            throw new ApiException("Họ tên từ 2–60 ký tự", "INVALID_NAME");
        }
        if (!hoTen.matches(".*[\\p{L}].*")) {
            throw new ApiException(
                    "Họ tên không được toàn số hoặc ký tự đặc biệt",
                    "INVALID_NAME");
        }

        String sdt = req.getSoDienThoai() != null
                ? req.getSoDienThoai().replaceAll("\\D", "")
                : "";
        if (!sdt.matches("^(0[35789])\\d{8}$")) {
            throw new ApiException(
                    "Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)",
                    "INVALID_PHONE");
        }

        String email = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ApiException("Email không hợp lệ", "INVALID_EMAIL");
        }

        String tenCongTy = blankToNull(req.getTenCongTy());
        if (tenCongTy != null && tenCongTy.length() > 100) {
            throw new ApiException("Tên công ty tối đa 100 ký tự", "INVALID_COMPANY");
        }

        String ghiChu = blankToNull(req.getGhiChu());
        if (ghiChu != null && ghiChu.length() > 500) {
            throw new ApiException("Ghi chú tối đa 500 ký tự", "INVALID_NOTE");
        }

        LocalDateTime since = LocalDateTime.now().minusMinutes(SPAM_WINDOW_MINUTES);
        if (repository.existsRecentDuplicate(sdt, ct.getId(), since)) {
            throw new ApiException(
                    "Yêu cầu đã được gửi, vui lòng chờ liên hệ",
                    "DUPLICATE_REQUEST");
        }

        YeuCauMuaSoLuongLon y = new YeuCauMuaSoLuongLon();
        y.setIdChiTietSanPham(ct);
        y.setSoLuong(req.getSoLuong());
        y.setTenCongTy(tenCongTy);
        y.setHoTen(hoTen);
        y.setSoDienThoai(sdt);
        y.setEmail(email);
        y.setGhiChu(ghiChu);
        y.setNhanKhuyenMai(Boolean.TRUE.equals(req.getNhanKhuyenMai()));
        y.setTrangThai("MOI");
        y.setNgayTao(LocalDateTime.now());

        Integer idKhach = optionalKhachHangId();
        if (idKhach != null) {
            KhachHang kh = khachHangRepository.findById(idKhach).orElse(null);
            y.setIdKhachHang(kh);
        }

        YeuCauMuaSoLuongLon saved = repository.save(y);

        String tenSp = ct.getSanPham() != null ? ct.getSanPham().getTen() : ("SKU " + ct.getSku());
        thongBaoService.taoThongBao(
                LoaiThongBao.YEU_CAU_MUA_SO_LUONG_LON,
                "Yêu cầu mua số lượng lớn mới",
                "Yêu cầu mua số lượng lớn mới: " + tenSp + " x " + saved.getSoLuong(),
                "/admin/yeu-cau-mua-so-luong-lon",
                saved.getId(),
                String.valueOf(saved.getId()));

        return YeuCauMuaSoLuongLonResponse.from(
                repository.findDetailById(saved.getId()).orElse(saved));
    }

    @Transactional(readOnly = true)
    public Page<YeuCauMuaSoLuongLonResponse> danhSach(String trangThai, int page, int size) {
        String tt = trangThai != null && !trangThai.isBlank() ? trangThai.trim() : null;
        if (tt != null && !TRANG_THAI_HOP_LE.contains(tt)) {
            throw new ApiException("Trạng thái không hợp lệ", "INVALID_STATUS");
        }
        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 50),
                Sort.by(Sort.Direction.DESC, "ngayTao"));
        return repository.search(tt, pageable).map(y -> {
            // trigger lazy load trong transaction
            if (y.getIdChiTietSanPham() != null) {
                y.getIdChiTietSanPham().getSku();
                if (y.getIdChiTietSanPham().getSanPham() != null) {
                    y.getIdChiTietSanPham().getSanPham().getTen();
                }
                if (y.getIdChiTietSanPham().getMauSac() != null) {
                    y.getIdChiTietSanPham().getMauSac().getTen();
                }
            }
            if (y.getIdNhanVienXuLy() != null) {
                y.getIdNhanVienXuLy().getHoTen();
            }
            return YeuCauMuaSoLuongLonResponse.from(y);
        });
    }

    @Transactional(readOnly = true)
    public YeuCauMuaSoLuongLonResponse chiTiet(Integer id) {
        return repository.findDetailById(id)
                .map(YeuCauMuaSoLuongLonResponse::from)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu", "NOT_FOUND"));
    }

    @Transactional(readOnly = true)
    public long demMoi() {
        return repository.countByTrangThai("MOI");
    }

    @Transactional
    public YeuCauMuaSoLuongLonResponse capNhat(Integer id, CapNhatYeuCauMuaSoLuongLonRequest req) {
        String tt = req.getTrangThai() != null ? req.getTrangThai().trim() : "";
        if (!TRANG_THAI_HOP_LE.contains(tt)) {
            throw new ApiException("Trạng thái không hợp lệ", "INVALID_STATUS");
        }
        YeuCauMuaSoLuongLon y = repository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu", "NOT_FOUND"));
        y.setTrangThai(tt);
        y.setGhiChuNoiBo(blankToNull(req.getGhiChuNoiBo()));

        Integer idNv = SecurityUtils.currentNhanVienId();
        NhanVien nv = nhanVienRepository.findById(idNv).orElse(null);
        y.setIdNhanVienXuLy(nv);

        repository.save(y);
        return YeuCauMuaSoLuongLonResponse.from(
                repository.findDetailById(id).orElse(y));
    }

    private Integer optionalKhachHangId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return null;
        }
        boolean isKhach = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> "ROLE_KHACH_HANG".equals(a));
        if (!isKhach) {
            return null;
        }
        try {
            return Integer.parseInt(auth.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String blankToNull(String s) {
        return normalizeSpaces(s);
    }

    private static String normalizeSpaces(String s) {
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? null : t;
    }
}
