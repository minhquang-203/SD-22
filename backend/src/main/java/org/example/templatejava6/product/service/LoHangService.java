package org.example.templatejava6.product.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.entity.HoaDonChiTietLo;
import org.example.templatejava6.order.repository.HoaDonChiTietLoRepository;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.LoHang;
import org.example.templatejava6.product.model.request.LoHangRequest;
import org.example.templatejava6.product.model.response.LoHangResponse;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.product.repository.LoHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LoHangService {

    public record PhanBoLo(Integer idLoHang, int soLuong) {}

    @Autowired private LoHangRepository loHangRepository;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private HoaDonChiTietLoRepository hoaDonChiTietLoRepository;

    @Transactional(readOnly = true)
    public List<LoHangResponse> listByChiTiet(Integer idChiTietSanPham) {
        getChiTietOrThrow(idChiTietSanPham);
        return loHangRepository.findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(idChiTietSanPham)
                .stream()
                .filter(l -> Boolean.TRUE.equals(l.getTrangThai()))
                .map(LoHangResponse::new)
                .toList();
    }

    @Transactional
    public LoHangResponse nhapLo(LoHangRequest request) {
        ChiTietSanPham ct = getChiTietOrThrow(request.getIdChiTietSanPham());
        if (!Boolean.TRUE.equals(ct.getTrangThai())) {
            throw new ApiException("Biến thể không còn hoạt động", "INACTIVE_SKU");
        }
        validateSoLuongVaHsd(request);

        LoHang lo = new LoHang();
        lo.setChiTietSanPham(ct);
        lo.setSoLo(request.getSoLo().trim());
        lo.setNgayNhap(request.getNgayNhap());
        lo.setHanSuDung(request.getHanSuDung());
        lo.setSoLuongNhap(request.getSoLuongNhap());
        lo.setSoLuongCon(request.getSoLuongNhap());
        lo.setSoLuongLoi(0);
        lo.setGhiChu(request.getGhiChu());
        lo.setTrangThai(true);
        lo = loHangRepository.save(lo);

        syncTonKho(ct.getId());
        return new LoHangResponse(lo);
    }

    /**
     * Trừ tồn theo FEFO và ghi nhận phân bổ lô cho dòng hóa đơn (để hoàn đúng lô).
     */
    @Transactional
    public void truTonVaGhiNhan(HoaDonChiTiet hoaDonChiTiet, int soLuong) {
        if (hoaDonChiTiet == null || hoaDonChiTiet.getId() == null) {
            throw new ApiException("Thiếu dòng hóa đơn để ghi nhận xuất lô.", "VALIDATION_ERROR");
        }
        Integer idCts = hoaDonChiTiet.getIdChiTietSanPham() != null
                ? hoaDonChiTiet.getIdChiTietSanPham().getId() : null;
        if (idCts == null) {
            throw new ApiException("Thiếu biến thể sản phẩm trên dòng hóa đơn.", "VALIDATION_ERROR");
        }

        List<PhanBoLo> phanBo = truTonTheoFefo(idCts, soLuong);
        for (PhanBoLo item : phanBo) {
            LoHang lot = loHangRepository.findById(item.idLoHang())
                    .orElseThrow(() -> new ApiException("Không tìm thấy lô hàng.", "LOT_NOT_FOUND"));
            HoaDonChiTietLo row = new HoaDonChiTietLo();
            row.setHoaDonChiTiet(hoaDonChiTiet);
            row.setLoHang(lot);
            row.setSoLuong(item.soLuong());
            hoaDonChiTietLoRepository.save(row);
        }
    }

    /**
     * Sửa nhẹ thông tin lô (HSD + ghi chú). Không đổi số lượng / số lô / ngày nhập.
     * Lô chỉ được tạo qua phiếu nhập.
     */
    @Transactional
    public LoHangResponse capNhatThongTinNhe(Integer id, LocalDate hanSuDung, String ghiChu) {
        LoHang lo = getLoOrThrow(id);
        if (!Boolean.TRUE.equals(lo.getTrangThai())) {
            throw new ApiException("Lô hàng không còn hoạt động", "NOT_FOUND");
        }
        if (hanSuDung != null && lo.getNgayNhap() != null && !hanSuDung.isAfter(lo.getNgayNhap())) {
            throw new ApiException("Hạn sử dụng phải sau ngày nhập", "VALIDATION_ERROR");
        }
        lo.setHanSuDung(hanSuDung);
        lo.setGhiChu(ghiChu != null && !ghiChu.isBlank() ? ghiChu.trim() : null);
        return new LoHangResponse(loHangRepository.save(lo));
    }

    @Transactional
    public LoHangResponse capNhatLo(Integer id, LoHangRequest request) {
        // Giữ tương thích: chỉ cập nhật HSD + ghi chú (hướng A — không sửa SL qua màn lô)
        return capNhatThongTinNhe(id, request.getHanSuDung(), request.getGhiChu());
    }

    @Transactional
    public void xoaLo(Integer id) {
        throw new ApiException(
                "Không xóa lô trực tiếp. Lô được quản lý qua phiếu nhập.",
                "FORBIDDEN");
    }

    /**
     * Trừ tồn theo FEFO. Lô được lấy bằng PESSIMISTIC_WRITE (FOR UPDATE)
     * nên các checkout cùng SKU sẽ serialize trong transaction.
     */
    @Transactional
    public List<PhanBoLo> truTonTheoFefo(Integer idChiTietSanPham, int soLuong) {
        if (soLuong <= 0) {
            return List.of();
        }
        List<LoHang> lots = loHangRepository.findAvailableForFefo(idChiTietSanPham);
        List<PhanBoLo> phanBo = new ArrayList<>();
        int remaining = soLuong;
        for (LoHang lot : lots) {
            if (remaining <= 0) {
                break;
            }
            int available = lot.getSoLuongCon() != null ? lot.getSoLuongCon() : 0;
            int deduct = Math.min(available, remaining);
            if (deduct <= 0) {
                continue;
            }
            lot.setSoLuongCon(available - deduct);
            loHangRepository.save(lot);
            phanBo.add(new PhanBoLo(lot.getId(), deduct));
            remaining -= deduct;
        }
        if (remaining > 0) {
            ChiTietSanPham ct = getChiTietOrThrow(idChiTietSanPham);
            throw new ApiException(
                    "Không đủ tồn kho cho SKU " + ct.getSku() + " (thiếu " + remaining + ").",
                    "OUT_OF_STOCK");
        }
        syncTonKho(idChiTietSanPham);
        return phanBo;
    }

    /**
     * Hoàn tồn về đúng một lô (hàng còn tốt khi trả hàng).
     */
    @Transactional
    public void hoanTonVaoLo(Integer idLoHang, int soLuong) {
        if (idLoHang == null || soLuong <= 0) {
            throw new ApiException("Số lượng hoàn về lô không hợp lệ.", "VALIDATION_ERROR");
        }
        LoHang lot = loHangRepository.findByIdForUpdate(idLoHang)
                .orElseThrow(() -> new ApiException("Không tìm thấy lô hàng.", "LOT_NOT_FOUND"));
        int current = lot.getSoLuongCon() != null ? lot.getSoLuongCon() : 0;
        lot.setSoLuongCon(current + soLuong);
        loHangRepository.save(lot);
        Integer idCts = lot.getChiTietSanPham() != null ? lot.getChiTietSanPham().getId() : null;
        if (idCts != null) {
            syncTonKho(idCts);
        }
    }

    /**
     * Ghi nhận hàng lỗi/hỏng khi trả — tăng soLuongLoi, KHÔNG vào tồn bán được.
     */
    @Transactional
    public void ghiNhanHangLoi(Integer idLoHang, int soLuong) {
        if (idLoHang == null || soLuong <= 0) {
            throw new ApiException("Số lượng hàng lỗi không hợp lệ.", "VALIDATION_ERROR");
        }
        LoHang lot = loHangRepository.findByIdForUpdate(idLoHang)
                .orElseThrow(() -> new ApiException("Không tìm thấy lô hàng.", "LOT_NOT_FOUND"));
        int current = lot.getSoLuongLoi() != null ? lot.getSoLuongLoi() : 0;
        lot.setSoLuongLoi(current + soLuong);
        loHangRepository.save(lot);
    }

    /** Tham chiếu lô đã tồn tại (dùng gắn FK chi tiết trả hàng). */
    @Transactional(readOnly = true)
    public LoHang getLoHangRef(Integer idLoHang) {
        return loHangRepository.findById(idLoHang)
                .orElseThrow(() -> new ApiException("Không tìm thấy lô hàng.", "LOT_NOT_FOUND"));
    }

    /**
     * Hoàn tồn đúng các lô đã xuất cho dòng hóa đơn.
     * Đơn cũ không có phân bổ: fallback cộng vào lô còn chỗ (ưu tiên FEFO).
     */
    @Transactional
    public void hoanTonTheoChiTiet(HoaDonChiTiet hoaDonChiTiet) {
        if (hoaDonChiTiet == null || hoaDonChiTiet.getSoLuong() == null || hoaDonChiTiet.getSoLuong() <= 0) {
            return;
        }
        Integer idCts = hoaDonChiTiet.getIdChiTietSanPham() != null
                ? hoaDonChiTiet.getIdChiTietSanPham().getId() : null;
        if (idCts == null) {
            return;
        }

        List<HoaDonChiTietLo> rows = hoaDonChiTietLoRepository.findByHoaDonChiTiet(hoaDonChiTiet);
        if (rows.isEmpty()) {
            hoanTon(idCts, hoaDonChiTiet.getSoLuong());
            return;
        }

        for (HoaDonChiTietLo row : rows) {
            Integer idLo = row.getLoHang() != null ? row.getLoHang().getId() : null;
            if (idLo == null) {
                continue;
            }
            LoHang lot = loHangRepository.findByIdForUpdate(idLo)
                    .orElseThrow(() -> new ApiException("Không tìm thấy lô hàng.", "LOT_NOT_FOUND"));
            int current = lot.getSoLuongCon() != null ? lot.getSoLuongCon() : 0;
            int add = row.getSoLuong() != null ? row.getSoLuong() : 0;
            if (add > 0) {
                lot.setSoLuongCon(current + add);
                loHangRepository.save(lot);
            }
        }
        hoaDonChiTietLoRepository.deleteByHoaDonChiTiet(hoaDonChiTiet);
        syncTonKho(idCts);
    }

    /**
     * Fallback hoàn tồn khi không có phân bổ lô (đơn cũ):
     * ưu tiên đổ lại vào các lô FEFO còn chỗ (soLuongCon &lt; soLuongNhap), phần thừa vào lô mới nhất.
     */
    @Transactional
    public void hoanTon(Integer idChiTietSanPham, int soLuong) {
        if (soLuong <= 0) {
            return;
        }
        List<LoHang> lots = loHangRepository.findActiveForRestock(idChiTietSanPham);
        if (lots.isEmpty()) {
            ChiTietSanPham ct = getChiTietOrThrow(idChiTietSanPham);
            throw new ApiException("Không tìm thấy lô hàng để hoàn tồn cho SKU " + ct.getSku() + ".", "LOT_NOT_FOUND");
        }

        int remaining = soLuong;
        for (LoHang lot : lots) {
            if (remaining <= 0) {
                break;
            }
            int current = lot.getSoLuongCon() != null ? lot.getSoLuongCon() : 0;
            int nhap = lot.getSoLuongNhap() != null ? lot.getSoLuongNhap() : current;
            int room = Math.max(0, nhap - current);
            if (room <= 0) {
                continue;
            }
            int add = Math.min(room, remaining);
            lot.setSoLuongCon(current + add);
            loHangRepository.save(lot);
            remaining -= add;
        }

        if (remaining > 0) {
            LoHang newest = lots.stream()
                    .max(Comparator.comparing(LoHang::getId))
                    .orElse(lots.get(0));
            int current = newest.getSoLuongCon() != null ? newest.getSoLuongCon() : 0;
            newest.setSoLuongCon(current + remaining);
            loHangRepository.save(newest);
        }
        syncTonKho(idChiTietSanPham);
    }

    @Transactional
    public void syncTonKho(Integer idChiTietSanPham) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdForUpdate(idChiTietSanPham)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
        int total = loHangRepository.sumSoLuongCon(idChiTietSanPham);
        ct.setSoLuongTon(total);
        chiTietSanPhamRepository.save(ct);
    }

    @Transactional(readOnly = true)
    public LocalDate nearestExpiry(Integer idChiTietSanPham) {
        return loHangRepository.findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(idChiTietSanPham)
                .stream()
                .filter(l -> Boolean.TRUE.equals(l.getTrangThai()))
                .filter(l -> l.getSoLuongCon() != null && l.getSoLuongCon() > 0)
                .map(LoHang::getHanSuDung)
                .filter(d -> d != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean hasSapHetHan(Integer idChiTietSanPham) {
        return loHangRepository.findByChiTietSanPham_IdOrderByNgayNhapDescHanSuDungAsc(idChiTietSanPham)
                .stream()
                .anyMatch(l -> Boolean.TRUE.equals(l.getTrangThai())
                        && l.getSoLuongCon() != null
                        && l.getSoLuongCon() > 0
                        && LoHangResponse.isSapHetHan(l.getHanSuDung()));
    }

    private ChiTietSanPham getChiTietOrThrow(Integer id) {
        return chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm", "NOT_FOUND"));
    }

    private LoHang getLoOrThrow(Integer id) {
        return loHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy lô hàng", "NOT_FOUND"));
    }

    private void validateSoLuongVaHsd(LoHangRequest request) {
        if (request.getSoLuongNhap() == null || request.getSoLuongNhap() <= 0) {
            throw new ApiException("Số lượng nhập phải lớn hơn 0", "VALIDATION_ERROR");
        }
        if (request.getHanSuDung() != null && request.getNgayNhap() != null
                && !request.getHanSuDung().isAfter(request.getNgayNhap())) {
            throw new ApiException("Hạn sử dụng phải sau ngày nhập", "VALIDATION_ERROR");
        }
    }
}
