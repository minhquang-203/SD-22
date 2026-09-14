package org.example.templatejava6.voucher.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.PhamViPhieuGiamGia;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.model.response.KhachHangResponse;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.voucher.entity.KhachHangPhieuGiamGia;
import org.example.templatejava6.voucher.model.request.GanVoucherTheoNhomRequest;
import org.example.templatejava6.voucher.model.response.PhieuGiamGiaResponse;
import org.example.templatejava6.voucher.repository.KhachHangPhieuGiamGiaRepository;
import org.example.templatejava6.voucher.repository.PhieuGiamGiaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoucherKhachHangService {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final KhachHangPhieuGiamGiaRepository khachHangPhieuGiamGiaRepository;
    private final KhachHangRepository khachHangRepository;

    public VoucherKhachHangService(
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            KhachHangPhieuGiamGiaRepository khachHangPhieuGiamGiaRepository,
            KhachHangRepository khachHangRepository) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.khachHangPhieuGiamGiaRepository = khachHangPhieuGiamGiaRepository;
        this.khachHangRepository = khachHangRepository;
    }

    /* ===================== ADMIN ===================== */

    /** Gán voucher cho danh sách khách hàng cụ thể. Trả về số khách được gán mới. */
    @Transactional
    public int ganChoKhachHang(Integer idVoucher, List<Integer> idKhachHangs) {
        PhieuGiamGia voucher = getVoucherOrThrow(idVoucher);
        if (idKhachHangs == null || idKhachHangs.isEmpty()) {
            return 0;
        }
        List<KhachHang> khachHangs = khachHangRepository.findAllById(idKhachHangs);
        if (coKhoangDiem(voucher)) {
            List<KhachHang> hopLe = new ArrayList<>();
            for (KhachHang kh : khachHangs) {
                if (kh == null) {
                    continue;
                }
                if (!diemKhopVoucher(kh, voucher)) {
                    throw new ApiException(
                            "Khách \"" + (kh.getHoTen() != null ? kh.getHoTen() : kh.getId())
                                    + "\" không đủ điểm theo khoảng của voucher.",
                            "DIEM_KHONG_KHOP");
                }
                hopLe.add(kh);
            }
            return luuGan(voucher, hopLe);
        }
        return luuGan(voucher, khachHangs);
    }

    /**
     * Tự gán voucher cá nhân cho mọi khách đang đủ điểm (khi tạo/cập nhật phiếu có khoảng điểm).
     * Không làm gì nếu phiếu không phải CA_NHAN hoặc không cấu hình điểm.
     */
    @Transactional
    public int ganTuDongTheoKhoangDiem(PhieuGiamGia voucher) {
        if (voucher == null || voucher.getId() == null) {
            return 0;
        }
        if (voucher.getPhamVi() != PhamViPhieuGiamGia.CA_NHAN || !coKhoangDiem(voucher)) {
            return 0;
        }
        List<KhachHang> khachHangs = khachHangRepository.locTheoKhoangDiem(
                voucher.getDiemToiThieu(),
                voucher.getDiemToiDa());
        if (khachHangs.isEmpty()) {
            return 0;
        }
        return luuGan(voucher, khachHangs);
    }

    /**
     * Khi khách được cộng điểm: tự nhận các voucher cá nhân còn hiệu lực khớp khoảng điểm.
     */
    @Transactional
    public int tuDongGanKhiCapNhatDiem(KhachHang khachHang) {
        if (khachHang == null || khachHang.getId() == null) {
            return 0;
        }
        int diem = khachHang.getDiemTichLuy() != null ? khachHang.getDiemTichLuy() : 0;
        List<PhieuGiamGia> vouchers = phieuGiamGiaRepository.findCaNhanAutoTheoDiem(diem);
        int total = 0;
        for (PhieuGiamGia voucher : vouchers) {
            total += luuGan(voucher, List.of(khachHang));
        }
        return total;
    }

    public static boolean coKhoangDiem(PhieuGiamGia voucher) {
        return voucher != null
                && (voucher.getDiemToiThieu() != null || voucher.getDiemToiDa() != null);
    }

    public static boolean diemKhopVoucher(KhachHang kh, PhieuGiamGia voucher) {
        if (kh == null || voucher == null || !coKhoangDiem(voucher)) {
            return true;
        }
        int diem = kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0;
        if (voucher.getDiemToiThieu() != null && diem < voucher.getDiemToiThieu()) {
            return false;
        }
        if (voucher.getDiemToiDa() != null && diem > voucher.getDiemToiDa()) {
            return false;
        }
        return true;
    }

    /** Gán voucher theo bộ lọc nhóm. Trả về số khách được gán mới. */
    @Transactional
    public int ganTheoNhom(Integer idVoucher, GanVoucherTheoNhomRequest request) {
        PhieuGiamGia voucher = getVoucherOrThrow(idVoucher);
        List<KhachHang> khachHangs = locKhachTheoNhom(request);
        if (khachHangs.isEmpty()) {
            throw new ApiException("Không có khách hàng nào khớp với bộ lọc.", "NO_CUSTOMER_MATCH");
        }
        return luuGan(voucher, khachHangs);
    }

    /** Xem trước danh sách khách hàng khớp bộ lọc nhóm (không lưu). */
    @Transactional(readOnly = true)
    public List<KhachHangResponse> previewNhom(GanVoucherTheoNhomRequest request) {
        return locKhachTheoNhom(request).stream()
                .map(KhachHangResponse::new)
                .toList();
    }

    /** Danh sách khách hàng đã được gán một voucher. */
    @Transactional(readOnly = true)
    public List<KhachHangResponse> danhSachKhachDaGan(Integer idVoucher) {
        getVoucherOrThrow(idVoucher);
        return khachHangPhieuGiamGiaRepository.findByPhieuGiamGia_Id(idVoucher).stream()
                .map(KhachHangPhieuGiamGia::getKhachHang)
                .map(KhachHangResponse::new)
                .toList();
    }

    @Transactional
    public void boGan(Integer idVoucher, Integer idKhachHang) {
        khachHangPhieuGiamGiaRepository.deleteByPhieuGiamGia_IdAndKhachHang_Id(idVoucher, idKhachHang);
    }

    private int luuGan(PhieuGiamGia voucher, List<KhachHang> khachHangs) {
        int count = 0;
        for (KhachHang kh : khachHangs) {
            if (kh == null) {
                continue;
            }
            if (khachHangPhieuGiamGiaRepository
                    .existsByKhachHang_IdAndPhieuGiamGia_Id(kh.getId(), voucher.getId())) {
                continue;
            }
            KhachHangPhieuGiamGia lienKet = new KhachHangPhieuGiamGia();
            lienKet.setKhachHang(kh);
            lienKet.setPhieuGiamGia(voucher);
            lienKet.setDaSuDung(false);
            lienKet.setNgayGan(LocalDateTime.now());
            khachHangPhieuGiamGiaRepository.save(lienKet);
            count++;
        }
        return count;
    }

    private List<KhachHang> locKhachTheoNhom(GanVoucherTheoNhomRequest request) {
        LocalDateTime tuNgayTao = null;
        if (request.getSoNgayKhachMoi() != null && request.getSoNgayKhachMoi() > 0) {
            tuNgayTao = LocalDateTime.now().minusDays(request.getSoNgayKhachMoi());
        }
        String gioiTinh = (request.getGioiTinh() != null && !request.getGioiTinh().isBlank())
                ? request.getGioiTinh().trim()
                : null;
        return khachHangRepository.locTheoNhom(
                request.getDiemToiThieu(),
                tuNgayTao,
                request.getThangSinhNhat(),
                gioiTinh,
                request.getIdLoaiDa());
    }

    /* ===================== KHÁCH HÀNG ===================== */

    /** Voucher công khai đang hiệu lực (tab "Tất cả voucher"). */
    @Transactional(readOnly = true)
    public List<PhieuGiamGiaResponse> voucherCongKhai(String keyword) {
        String normalized = keyword == null ? null : keyword.trim();
        return phieuGiamGiaRepository
                .findPublicAvailableForCustomer(normalized, Pageable.unpaged())
                .map(PhieuGiamGiaResponse::new)
                .getContent();
    }

    /** Voucher cá nhân đã được gán cho khách đang đăng nhập (tab "Voucher cá nhân"). */
    @Transactional(readOnly = true)
    public List<PhieuGiamGiaResponse> voucherCaNhanCuaToi() {
        Integer idKhach = getIdKhachDangNhap();
        return khachHangPhieuGiamGiaRepository.findVoucherByKhachHangId(idKhach).stream()
                .map(PhieuGiamGiaResponse::new)
                .toList();
    }

    /**
     * Kiểm tra khách có được phép dùng voucher không. Với voucher cá nhân,
     * khách phải nằm trong danh sách được gán. Dùng ở luồng checkout.
     */
    @Transactional(readOnly = true)
    public boolean khachDuocDungVoucher(Integer idKhachHang, PhieuGiamGia voucher) {
        if (voucher == null) {
            return false;
        }
        if (voucher.getPhamVi() != PhamViPhieuGiamGia.CA_NHAN) {
            return true;
        }
        if (idKhachHang == null) {
            return false;
        }
        return khachHangPhieuGiamGiaRepository.daGanChoKhach(idKhachHang, voucher.getId());
    }

    private PhieuGiamGia getVoucherOrThrow(Integer id) {
        return phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));
    }

    private Integer getIdKhachDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ApiException("Chưa đăng nhập", "UNAUTHORIZED");
        }
        try {
            return Integer.parseInt(auth.getName());
        } catch (NumberFormatException ex) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
    }
}
