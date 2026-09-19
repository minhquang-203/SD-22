package org.example.templatejava6.order.service;

import org.example.templatejava6.common.enums.LoaiHangTra;
import org.example.templatejava6.order.entity.ChiTietTraHangLo;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.YeuCauTraHang;
import org.example.templatejava6.order.repository.ChiTietTraHangLoRepository;
import org.example.templatejava6.order.repository.HoaDonChiTietLoRepository;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.product.service.LoHangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Nhap kho khi tra hang — CHI thuc hien sau khi hoan tien thanh cong.
 *
 * <p>Luong tra hang tach viec "nhan lai hang" khoi viec "nhap kho": khi shop nhan hang,
 * nhan vien chi phan loai lo TOT/LOI va luu vao {@code chi_tiet_tra_hang_lo}, CHUA cham vao ton kho.
 * Chi khi admin quyet dinh hoan tien thanh cong thi hang moi thuc su nhap kho:
 * <ul>
 *   <li>TOT -> cong lai ton ban duoc ({@code so_luong_con}).</li>
 *   <li>LOI -> ghi nhan hang loi ({@code so_luong_loi}), khong vao ton ban duoc.</li>
 * </ul>
 * Neu admin tu choi hoan tien, hang duoc tra ve khach nen KHONG nhap kho — tranh viec shop vua
 * giu tien vua giu hang.
 */
@Service
public class ReturnStockService {

    private static final Logger log = LoggerFactory.getLogger(ReturnStockService.class);

    private final ChiTietTraHangLoRepository chiTietTraHangLoRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonChiTietLoRepository hoaDonChiTietLoRepository;
    private final LoHangService loHangService;

    public ReturnStockService(ChiTietTraHangLoRepository chiTietTraHangLoRepository,
                              HoaDonChiTietRepository hoaDonChiTietRepository,
                              HoaDonChiTietLoRepository hoaDonChiTietLoRepository,
                              LoHangService loHangService) {
        this.chiTietTraHangLoRepository = chiTietTraHangLoRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.hoaDonChiTietLoRepository = hoaDonChiTietLoRepository;
        this.loHangService = loHangService;
    }

    /**
     * Nhap kho cho mot yeu cau tra hang da duoc hoan tien thanh cong.
     * <ul>
     *   <li>Co phan bo lo ({@code chi_tiet_tra_hang_lo}): TOT hoan ve lo, LOI ghi hang loi;
     *       sau do xoa lien ket lo cua don goc.</li>
     *   <li>Don cu khong co phan bo lo: fallback hoan tat ca nhu hang tot.</li>
     * </ul>
     */
    @Transactional
    public void nhapKhoKhiHoanTien(YeuCauTraHang yeuCau) {
        if (yeuCau == null || yeuCau.getId() == null) {
            return;
        }
        HoaDon hoaDon = yeuCau.getIdHoaDon();
        List<ChiTietTraHangLo> phanBo =
                chiTietTraHangLoRepository.findByYeuCauTraHang_IdOrderByIdAsc(yeuCau.getId());

        if (phanBo.isEmpty()) {
            // Don cu khong co phan bo lo: hoan tat ca nhu hang tot (tu xoa lien ket lo neu co).
            if (hoaDon != null) {
                for (HoaDonChiTiet ct : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
                    loHangService.hoanTonTheoChiTiet(ct);
                }
            }
            return;
        }

        for (ChiTietTraHangLo item : phanBo) {
            Integer idLo = item.getLoHang() != null ? item.getLoHang().getId() : null;
            int soLuong = item.getSoLuong() != null ? item.getSoLuong() : 0;
            if (idLo == null || soLuong <= 0) {
                continue;
            }
            if (item.getLoai() == LoaiHangTra.TOT) {
                loHangService.hoanTonVaoLo(idLo, soLuong);
            } else {
                loHangService.ghiNhanHangLoi(idLo, soLuong);
            }
        }

        // Da nhap kho theo phan bo tra -> xoa lien ket lo cua don goc de tranh hoan ton lan 2.
        if (hoaDon != null) {
            Set<Integer> cleared = new HashSet<>();
            for (HoaDonChiTiet ct : hoaDonChiTietRepository.findByIdHoaDon(hoaDon)) {
                if (ct.getId() != null && cleared.add(ct.getId())) {
                    hoaDonChiTietLoRepository.deleteByHoaDonChiTiet(ct);
                }
            }
        }
        log.info("Da nhap kho cho yeu cau tra hang #{} sau khi hoan tien thanh cong.", yeuCau.getId());
    }
}
