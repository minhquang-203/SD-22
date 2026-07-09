package org.example.templatejava6.order.service;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Dong bo trang thai don hang noi bo theo trang thai van don ben Giao Hang Nhanh (GHN).
 *
 * <p>Anh xa status GHN sang {@link TrangThaiDonHang} va chi cho phep chuyen tien (khong lui).
 * Moi lan chuyen trang thai se ghi them mot ban ghi {@code lich_su_don_hang}.</p>
 */
@Service
public class GhnOrderSyncService {

    private static final Logger log = LoggerFactory.getLogger(GhnOrderSyncService.class);

    private final HoaDonRepository hoaDonRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;
    private final GhnTrackingService ghnTrackingService;

    public GhnOrderSyncService(HoaDonRepository hoaDonRepository,
                               LichSuDonHangRepository lichSuDonHangRepository,
                               GhnTrackingService ghnTrackingService) {
        this.hoaDonRepository = hoaDonRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
        this.ghnTrackingService = ghnTrackingService;
    }

    /**
     * Dong bo mot don theo id bang cach goi API GHN lay status hien tai.
     */
    @Transactional
    public KetQuaDongBo dongBoTheoId(Integer id) {
        Optional<HoaDon> opt = hoaDonRepository.findById(id);
        if (opt.isEmpty()) {
            return KetQuaDongBo.boQua(id, null, "Khong tim thay don hang.");
        }
        return dongBo(opt.get());
    }

    /**
     * Gia lap webhook GHN: nhan status van don va cap nhat don theo mapping noi bo.
     */
    @Transactional
    public KetQuaDongBo giaLapWebhookTheoId(Integer id, String ghnStatus, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("Khong tim thay don hang.", "NOT_FOUND"));

        String maVanDon = hoaDon.getMaVanDonGhn();
        if (maVanDon == null || maVanDon.isBlank()) {
            throw new ApiException(
                    "Don chua co ma van don GHN, khong the gia lap webhook.",
                    "GHN_NO_ORDER_CODE");
        }

        String status = normalizeGhnStatus(ghnStatus);
        if (status == null) {
            throw new ApiException("Trang thai GHN khong hop le.", "VALIDATION_ERROR");
        }

        return apDungTrangThaiGhn(hoaDon, status, buildWebhookGhiChu(status, ghiChu));
    }

    /**
     * Dong bo trang thai cho mot hoa don da nap san (goi API GHN).
     */
    @Transactional
    public KetQuaDongBo dongBo(HoaDon hoaDon) {
        String maVanDon = hoaDon.getMaVanDonGhn();
        if (maVanDon == null || maVanDon.isBlank()) {
            return KetQuaDongBo.boQua(hoaDon.getId(), hoaDon.getTrangThai(), "Don chua co ma van don GHN.");
        }

        TrangThaiDonHang trangThaiHienTai = hoaDon.getTrangThai();
        if (trangThaiHienTai != null && trangThaiHienTai.laTrangThaiKetThuc()) {
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai, "Don da ket thuc vong doi.");
        }

        Optional<GhnTrackingService.TrackingInfo> tracking = ghnTrackingService.track(maVanDon);
        if (tracking.isEmpty()) {
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai,
                    "Khong lay duoc trang thai van don tu GHN.");
        }

        GhnTrackingService.TrackingInfo info = tracking.get();
        return apDungTrangThaiGhn(hoaDon, info.status(), "Tu dong cap nhat tu GHN: " + moTaGhn(info));
    }

    /**
     * Ap dung status GHN (tu API hoac webhook gia lap) len don hang noi bo.
     */
    @Transactional
    public KetQuaDongBo apDungTrangThaiGhn(HoaDon hoaDon, String ghnStatus, String ghiChu) {
        TrangThaiDonHang trangThaiHienTai = hoaDon.getTrangThai();
        if (trangThaiHienTai != null && trangThaiHienTai.laTrangThaiKetThuc()) {
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai, "Don da ket thuc vong doi.");
        }

        String status = normalizeGhnStatus(ghnStatus);
        TrangThaiDonHang trangThaiMoi = anhXa(status);
        String moTa = moTaGhnStatus(status);

        if (trangThaiMoi == null) {
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai,
                    "GHN: " + moTa + " (can xu ly thu cong).");
        }

        if (trangThaiHienTai == null || !trangThaiHienTai.coTheChuyenSang(trangThaiMoi)) {
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai,
                    "Khong can chuyen (" + moTa + ").");
        }

        hoaDon.setTrangThai(trangThaiMoi);
        hoaDonRepository.save(hoaDon);
        ghiLichSu(hoaDon, trangThaiMoi, ghiChu != null ? ghiChu : moTa);

        log.info("Cap nhat tu GHN: don {} {} -> {} (GHN status={})",
                hoaDon.getMaHoaDon(), trangThaiHienTai, trangThaiMoi, status);
        return KetQuaDongBo.daCapNhat(hoaDon.getId(), trangThaiHienTai, trangThaiMoi, moTa);
    }

    private void ghiLichSu(HoaDon hoaDon, TrangThaiDonHang trangThai, String ghiChu) {
        LichSuDonHang ls = new LichSuDonHang();
        ls.setIdHoaDon(hoaDon);
        ls.setTrangThai(trangThai.name());
        ls.setGhiChu(ghiChu != null && ghiChu.length() > 255 ? ghiChu.substring(0, 255) : ghiChu);
        ls.setThoiGian(LocalDateTime.now());
        lichSuDonHangRepository.save(ls);
    }

    private static String moTaGhn(GhnTrackingService.TrackingInfo info) {
        return moTaGhnStatus(info.status());
    }

    private static String moTaGhnStatus(String status) {
        String label = GhnTrackingService.labelOf(status);
        return (label != null ? label : status) + " (" + status + ")";
    }

    private static String buildWebhookGhiChu(String ghnStatus, String ghiChu) {
        String moTa = moTaGhnStatus(ghnStatus);
        if (ghiChu != null && !ghiChu.isBlank()) {
            return "Gia lap webhook GHN: " + moTa + " — " + ghiChu.trim();
        }
        return "Gia lap webhook GHN: " + moTa;
    }

    private static String normalizeGhnStatus(String ghnStatus) {
        if (ghnStatus == null) {
            return null;
        }
        String normalized = ghnStatus.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Anh xa trang thai van don GHN sang trang thai don hang noi bo.
     * Tra ve {@code null} voi cac trang thai can xu ly thu cong (giao that bai, su co...).
     */
    static TrangThaiDonHang anhXa(String ghnStatus) {
        if (ghnStatus == null) {
            return null;
        }
        return switch (ghnStatus) {
            case "ready_to_pick" -> TrangThaiDonHang.DANG_CHUAN_BI;
            case "picking", "money_collect_picking", "picked", "storing",
                 "transporting", "sorting", "delivering", "money_collect_delivering"
                    -> TrangThaiDonHang.DANG_GIAO;
            case "delivered" -> TrangThaiDonHang.HOAN_THANH;
            case "cancel" -> TrangThaiDonHang.DA_HUY;
            case "waiting_to_return", "return", "return_transporting", "return_sorting",
                 "returning", "return_fail", "returned"
                    -> TrangThaiDonHang.TRA_HANG;
            default -> null;
        };
    }

    /**
     * Ket qua dong bo cho mot don, dung cho endpoint dong bo / webhook gia lap.
     */
    public record KetQuaDongBo(
            Integer idHoaDon,
            boolean daCapNhat,
            TrangThaiDonHang trangThaiCu,
            TrangThaiDonHang trangThaiMoi,
            String thongDiep,
            String ghnStatus,
            String ghnStatusLabel
    ) {
        static KetQuaDongBo daCapNhat(
                Integer id,
                TrangThaiDonHang cu,
                TrangThaiDonHang moi,
                String moTaGhn
        ) {
            return new KetQuaDongBo(
                    id,
                    true,
                    cu,
                    moi,
                    "Da cap nhat sang '" + moi.getLabel() + "' theo GHN: " + moTaGhn,
                    extractGhnCode(moTaGhn),
                    extractGhnLabel(moTaGhn));
        }

        static KetQuaDongBo boQua(Integer id, TrangThaiDonHang hienTai, String thongDiep) {
            return new KetQuaDongBo(id, false, hienTai, hienTai, thongDiep, null, null);
        }

        private static String extractGhnCode(String moTaGhn) {
            if (moTaGhn == null) {
                return null;
            }
            int open = moTaGhn.lastIndexOf('(');
            int close = moTaGhn.lastIndexOf(')');
            if (open >= 0 && close > open) {
                return moTaGhn.substring(open + 1, close);
            }
            return null;
        }

        private static String extractGhnLabel(String moTaGhn) {
            if (moTaGhn == null) {
                return null;
            }
            int open = moTaGhn.lastIndexOf('(');
            if (open > 0) {
                return moTaGhn.substring(0, open).trim();
            }
            return moTaGhn;
        }
    }
}
