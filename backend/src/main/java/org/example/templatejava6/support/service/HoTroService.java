package org.example.templatejava6.support.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.realtime.service.HoTroRealtimeService;
import org.example.templatejava6.support.entity.PhienHoTro;
import org.example.templatejava6.support.entity.TinNhanHoTro;
import org.example.templatejava6.support.model.request.GuiTinHoTroRequest;
import org.example.templatejava6.support.model.response.PhienHoTroResponse;
import org.example.templatejava6.support.model.response.TinNhanHoTroResponse;
import org.example.templatejava6.support.repository.PhienHoTroRepository;
import org.example.templatejava6.support.repository.TinNhanHoTroRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoTroService {

    private static final String TRANG_THAI_MO = "MO";
    private static final String NGUOI_KHACH = "KHACH";
    private static final String NGUOI_NHAN_VIEN = "NHAN_VIEN";

    private final PhienHoTroRepository phienHoTroRepository;
    private final TinNhanHoTroRepository tinNhanHoTroRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final HoTroRealtimeService hoTroRealtimeService;

    public HoTroService(
            PhienHoTroRepository phienHoTroRepository,
            TinNhanHoTroRepository tinNhanHoTroRepository,
            KhachHangRepository khachHangRepository,
            NhanVienRepository nhanVienRepository,
            HoTroRealtimeService hoTroRealtimeService) {
        this.phienHoTroRepository = phienHoTroRepository;
        this.tinNhanHoTroRepository = tinNhanHoTroRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.hoTroRealtimeService = hoTroRealtimeService;
    }

    @Transactional
    public PhienHoTroResponse taoHoacLayPhienKhach() {
        KhachHang khach = getKhachDangNhap();
        PhienHoTro phien = phienHoTroRepository
                .findFirstByIdKhachHang_IdAndTrangThaiOrderByCapNhatCuoiDesc(khach.getId(), TRANG_THAI_MO)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    PhienHoTro moi = new PhienHoTro();
                    moi.setIdKhachHang(khach);
                    moi.setTrangThai(TRANG_THAI_MO);
                    moi.setNgayTao(now);
                    moi.setCapNhatCuoi(now);
                    return phienHoTroRepository.save(moi);
                });
        return PhienHoTroResponse.from(phien);
    }

    @Transactional
    public TinNhanHoTroResponse guiTinKhach(GuiTinHoTroRequest req) {
        KhachHang khach = getKhachDangNhap();
        String noiDung = requireNoiDung(req);
        PhienHoTro phien = resolvePhienChoKhach(req.getIdPhien(), khach);
        return luuVaPublish(phien, NGUOI_KHACH, khach.getId(), noiDung);
    }

    @Transactional(readOnly = true)
    public List<TinNhanHoTroResponse> lichSuTinNhanThongMinh(Integer idPhien) {
        if (laNhanVien()) {
            return lichSuTinNhanChoNhanVien(idPhien);
        }
        return lichSuTinNhanChoKhach(idPhien);
    }

    @Transactional(readOnly = true)
    public List<TinNhanHoTroResponse> lichSuTinNhanChoKhach(Integer idPhien) {
        KhachHang khach = getKhachDangNhap();
        PhienHoTro phien = loadPhien(idPhien);
        assertKhachSoHuu(phien, khach.getId());
        return tinNhanHoTroRepository.findByIdPhien_IdOrderByThoiGianAsc(idPhien).stream()
                .map(this::toTinResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PhienHoTroResponse> danhSachPhienMo() {
        List<PhienHoTro> list = phienHoTroRepository.findOpenSessions(TRANG_THAI_MO);
        return list.stream().map(phien -> {
            PhienHoTroResponse res = PhienHoTroResponse.from(phien);
            tinNhanHoTroRepository.findFirstByIdPhien_IdOrderByThoiGianDesc(phien.getId())
                    .ifPresent(tin -> {
                        res.setTinCuoi(tin.getNoiDung());
                        res.setNguoiGuiCuoi(tin.getNguoiGui());
                    });
            res.setSoTinChuaDoc(tinNhanHoTroRepository.countByIdPhien_IdAndNguoiGuiAndDaDocFalse(
                    phien.getId(), NGUOI_KHACH));
            return res;
        }).toList();
    }

    @Transactional
    public void danhDauDaDoc(Integer idPhien) {
        loadPhien(idPhien);
        tinNhanHoTroRepository.markDaDocByPhienAndNguoiGui(idPhien, NGUOI_KHACH);
    }

    @Transactional(readOnly = true)
    public List<TinNhanHoTroResponse> lichSuTinNhanChoNhanVien(Integer idPhien) {
        loadPhien(idPhien);
        return tinNhanHoTroRepository.findByIdPhien_IdOrderByThoiGianAsc(idPhien).stream()
                .map(this::toTinResponse)
                .toList();
    }

    @Transactional
    public TinNhanHoTroResponse traLoiNhanVien(Integer idPhien, GuiTinHoTroRequest req) {
        NhanVien nv = getNhanVienDangNhap();
        String noiDung = requireNoiDung(req);
        PhienHoTro phien = loadPhien(idPhien);
        if (!TRANG_THAI_MO.equalsIgnoreCase(phien.getTrangThai())) {
            throw new ApiException("Phiên hỗ trợ đã đóng.", "SESSION_CLOSED");
        }
        if (phien.getNguoiXuLy() == null) {
            phien.setNguoiXuLy(nv);
        }
        return luuVaPublish(phien, NGUOI_NHAN_VIEN, nv.getId(), noiDung);
    }

    private TinNhanHoTroResponse luuVaPublish(
            PhienHoTro phien, String nguoiGui, Integer idNguoiGui, String noiDung) {
        LocalDateTime now = LocalDateTime.now();
        TinNhanHoTro tin = new TinNhanHoTro();
        tin.setIdPhien(phien);
        tin.setNguoiGui(nguoiGui);
        tin.setIdNguoiGui(idNguoiGui);
        tin.setNoiDung(noiDung);
        tin.setDaDoc(false);
        tin.setThoiGian(now);
        tin = tinNhanHoTroRepository.save(tin);

        phien.setCapNhatCuoi(now);
        phienHoTroRepository.save(phien);

        // touch lazy fields before publish (after commit listener)
        if (phien.getIdKhachHang() != null) {
            phien.getIdKhachHang().getHoTen();
        }
        TinNhanHoTroResponse res = toTinResponse(tin);
        hoTroRealtimeService.publishTinNhanMoi(phien, tin, res);
        return res;
    }

    private TinNhanHoTroResponse toTinResponse(TinNhanHoTro tin) {
        TinNhanHoTroResponse res = TinNhanHoTroResponse.from(tin);
        if (NGUOI_NHAN_VIEN.equalsIgnoreCase(tin.getNguoiGui()) && tin.getIdNguoiGui() != null) {
            nhanVienRepository.findById(tin.getIdNguoiGui()).ifPresent(nv -> res.setTenNguoiGui(nv.getHoTen()));
        } else if (NGUOI_KHACH.equalsIgnoreCase(tin.getNguoiGui()) && tin.getIdNguoiGui() != null) {
            khachHangRepository.findById(tin.getIdNguoiGui()).ifPresent(kh -> res.setTenNguoiGui(kh.getHoTen()));
        }
        return res;
    }

    private PhienHoTro resolvePhienChoKhach(Integer idPhien, KhachHang khach) {
        if (idPhien != null) {
            PhienHoTro phien = loadPhien(idPhien);
            assertKhachSoHuu(phien, khach.getId());
            if (!TRANG_THAI_MO.equalsIgnoreCase(phien.getTrangThai())) {
                throw new ApiException("Phiên hỗ trợ đã đóng.", "SESSION_CLOSED");
            }
            return phien;
        }
        return phienHoTroRepository
                .findFirstByIdKhachHang_IdAndTrangThaiOrderByCapNhatCuoiDesc(khach.getId(), TRANG_THAI_MO)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    PhienHoTro moi = new PhienHoTro();
                    moi.setIdKhachHang(khach);
                    moi.setTrangThai(TRANG_THAI_MO);
                    moi.setNgayTao(now);
                    moi.setCapNhatCuoi(now);
                    return phienHoTroRepository.save(moi);
                });
    }

    private PhienHoTro loadPhien(Integer idPhien) {
        if (idPhien == null) {
            throw new ApiException("Thiếu id phiên hỗ trợ.", "MISSING_SESSION");
        }
        return phienHoTroRepository.findById(idPhien)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiên hỗ trợ.", "NOT_FOUND"));
    }

    private void assertKhachSoHuu(PhienHoTro phien, Integer idKhach) {
        Integer ownerId = phien.getIdKhachHang() != null ? phien.getIdKhachHang().getId() : null;
        if (ownerId == null || !ownerId.equals(idKhach)) {
            throw new ApiException("Bạn không có quyền truy cập phiên này.", "FORBIDDEN");
        }
    }

    private String requireNoiDung(GuiTinHoTroRequest req) {
        if (req == null || req.getNoiDung() == null || req.getNoiDung().isBlank()) {
            throw new ApiException("Nội dung tin nhắn không được trống.", "EMPTY_MESSAGE");
        }
        String noiDung = req.getNoiDung().trim();
        if (noiDung.length() > 2000) {
            throw new ApiException("Tin nhắn quá dài (tối đa 2000 ký tự).", "MESSAGE_TOO_LONG");
        }
        return noiDung;
    }

    private KhachHang getKhachDangNhap() {
        Integer id = SecurityUtils.currentKhachHangId();
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy tài khoản khách hàng.", "NOT_FOUND"));
    }

    private NhanVien getNhanVienDangNhap() {
        Integer id = SecurityUtils.currentNhanVienId();
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên.", "NOT_FOUND"));
    }

    private boolean laNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            String role = a.getAuthority();
            if ("ROLE_NHAN_VIEN".equals(role) || "ROLE_QUAN_LY".equals(role) || "ROLE_CHU".equals(role)) {
                return true;
            }
        }
        return false;
    }
}
