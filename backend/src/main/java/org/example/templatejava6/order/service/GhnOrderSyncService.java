package org.example.templatejava6.order.service;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.LichSuDonHang;
import org.example.templatejava6.order.repository.HoaDonRepository;
import org.example.templatejava6.order.repository.LichSuDonHangRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Tu dong dong bo trang thai don hang noi bo theo trang thai van don ben Giao Hang Nhanh (GHN).
 *
 * <p>Luong: lay cac don da co {@code maVanDonGhn} va chua o trang thai ket thuc, goi GHN de lay
 * trang thai van don, anh xa sang {@link TrangThaiDonHang} va chuyen tien (chi tien, khong lui).
 * Moi lan chuyen trang thai se ghi them mot ban ghi {@code lich_su_don_hang}.</p>
 */
@Service
public class GhnOrderSyncService {

    private static final Logger log = LoggerFactory.getLogger(GhnOrderSyncService.class);

    // Cac trang thai khong can dong bo nua (da ket thuc vong doi noi bo).
    private static final List<TrangThaiDonHang> TRANG_THAI_KET_THUC =
            List.of(TrangThaiDonHang.HOAN_THANH, TrangThaiDonHang.DA_HUY);

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
     * Lay id cac don can dong bo (co ma van don GHN, chua ket thuc vong doi).
     * Tra ve id thay vi entity de moi don duoc xu ly trong mot transaction rieng.
     */
    @Transactional(readOnly = true)
    public List<Integer> layIdDonCanDongBo() {
        return hoaDonRepository.findByMaVanDonGhnNotNullAndTrangThaiNotIn(TRANG_THAI_KET_THUC)
                .stream()
                .map(HoaDon::getId)
                .toList();
    }

    /**
     * Dong bo mot don theo id. Chay trong transaction rieng de loi cua mot don
     * khong lam hong cac don khac trong cung dot quet.
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
     * Dong bo trang thai cho mot hoa don da nap san.
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
        TrangThaiDonHang trangThaiMoi = anhXa(info.status());
        if (trangThaiMoi == null) {
            // Cac trang thai can xu ly thu cong (su co, giao that bai, hoan hang...): khong tu chuyen.
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai,
                    "GHN: " + moTaGhn(info) + " (can xu ly thu cong).");
        }

        if (trangThaiHienTai == null || !trangThaiHienTai.coTheChuyenSang(trangThaiMoi)) {
            // Cung trang thai hoac lui trang thai -> khong cap nhat lai (tranh ghi lich su trung lap).
            return KetQuaDongBo.boQua(hoaDon.getId(), trangThaiHienTai,
                    "Khong can chuyen (" + moTaGhn(info) + ").");
        }

        hoaDon.setTrangThai(trangThaiMoi);
        hoaDonRepository.save(hoaDon);
        ghiLichSu(hoaDon, trangThaiMoi, "Tu dong cap nhat tu GHN: " + moTaGhn(info));

        log.info("Dong bo GHN: don {} {} -> {} (GHN status={})",
                hoaDon.getMaHoaDon(), trangThaiHienTai, trangThaiMoi, info.status());
        return KetQuaDongBo.daCapNhat(hoaDon.getId(), trangThaiHienTai, trangThaiMoi, moTaGhn(info));
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
        String label = info.statusLabel() != null ? info.statusLabel() : info.status();
        return label + " (" + info.status() + ")";
    }

    /**
     * Anh xa trang thai van don GHN sang trang thai don hang noi bo.
     * Tra ve {@code null} voi cac trang thai can xu ly thu cong (giao that bai, hoan hang, su co).
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
            case "cancel", "returned" -> TrangThaiDonHang.DA_HUY;
            // Cac trang thai su co / cho hoan / hoan that bai: de nguyen cho nguoi xu ly thu cong.
            default -> null;
        };
    }

    /**
     * Ket qua dong bo cho mot don, dung cho endpoint dong bo thu cong va logging.
     */
    public record KetQuaDongBo(
            Integer idHoaDon,
            boolean daCapNhat,
            TrangThaiDonHang trangThaiCu,
            TrangThaiDonHang trangThaiMoi,
            String thongDiep
    ) {
        static KetQuaDongBo daCapNhat(Integer id, TrangThaiDonHang cu, TrangThaiDonHang moi, String moTaGhn) {
            return new KetQuaDongBo(id, true, cu, moi,
                    "Da cap nhat sang '" + moi.getLabel() + "' theo GHN: " + moTaGhn);
        }

        static KetQuaDongBo boQua(Integer id, TrangThaiDonHang hienTai, String thongDiep) {
            return new KetQuaDongBo(id, false, hienTai, hienTai, thongDiep);
        }
    }
}
