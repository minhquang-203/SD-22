package org.example.templatejava6.order.service;

import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.voucher.model.response.VariantSaleInfo;
import org.example.templatejava6.voucher.service.DotGiamGiaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CheckoutPricingService {

    private final DotGiamGiaService dotGiamGiaService;

    public CheckoutPricingService(DotGiamGiaService dotGiamGiaService) {
        this.dotGiamGiaService = dotGiamGiaService;
    }

    @Transactional(readOnly = true)
    public Map<Integer, VariantSaleInfo> loadActiveSales() {
        return dotGiamGiaService.getActiveSaleByVariantId();
    }

    public BigDecimal resolveDonGia(ChiTietSanPham chiTietSanPham, Map<Integer, VariantSaleInfo> saleMap) {
        if (chiTietSanPham == null || chiTietSanPham.getGiaBan() == null) {
            return null;
        }
        VariantSaleInfo sale = saleMap != null ? saleMap.get(chiTietSanPham.getId()) : null;
        if (sale != null && sale.getGiaSauGiam() != null) {
            return sale.getGiaSauGiam();
        }
        return chiTietSanPham.getGiaBan();
    }

    /**
     * Overload cho các luồng không phát sinh phí vận chuyển (VD: bán tại quầy).
     * Mã FREE_SHIP không áp dụng ở đây vì không có phí vận chuyển để miễn.
     */
    public BigDecimal tinhTienGiamPhieu(PhieuGiamGia phieu, BigDecimal tongTien) {
        if (phieu != null && phieu.getLoai() == LoaiPhieuGiamGia.FREE_SHIP) {
            throw new ApiException(
                    "Mã miễn phí vận chuyển chỉ áp dụng cho đơn giao hàng.", "INVALID_VOUCHER");
        }
        return tinhTienGiamPhieu(phieu, tongTien, BigDecimal.ZERO);
    }

    /**
     * Tính tiền giảm từ phiếu theo tổng giá trị đơn hàng (sau giá đợt giảm nếu có).
     * Không bị giới hạn bởi việc đơn có sản phẩm trong đợt giảm giá.
     *
     * <p>Với FREE_SHIP: số tiền giảm chính là phí vận chuyển (được miễn), giới hạn bởi
     * {@code giamToiDa} nếu có. Đơn tối thiểu {@code giaTriDonToiThieu} vẫn được áp dụng.
     */
    public BigDecimal tinhTienGiamPhieu(PhieuGiamGia phieu, BigDecimal tongTien, BigDecimal phiVanChuyen) {
        validatePhieu(phieu, tongTien);

        if (phieu.getLoai() == LoaiPhieuGiamGia.FREE_SHIP) {
            BigDecimal giamPhi = phiVanChuyen != null ? phiVanChuyen : BigDecimal.ZERO;
            if (phieu.getGiamToiDa() != null && giamPhi.compareTo(phieu.getGiamToiDa()) > 0) {
                giamPhi = phieu.getGiamToiDa();
            }
            return giamPhi.max(BigDecimal.ZERO);
        }

        BigDecimal giam;
        if (phieu.getLoai() == LoaiPhieuGiamGia.PHAN_TRAM) {
            giam = tongTien.multiply(phieu.getGiaTri())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (phieu.getGiamToiDa() != null && giam.compareTo(phieu.getGiamToiDa()) > 0) {
                giam = phieu.getGiamToiDa();
            }
        } else if (phieu.getLoai() == LoaiPhieuGiamGia.TIEN_MAT) {
            giam = phieu.getGiaTri();
        } else {
            throw new ApiException("Loại mã giảm giá không hợp lệ.", "INVALID_VOUCHER");
        }
        return giam.compareTo(tongTien) > 0 ? tongTien : giam;
    }

    private void validatePhieu(PhieuGiamGia phieu, BigDecimal tongTien) {
        if (!Boolean.TRUE.equals(phieu.getTrangThai())) {
            throw new ApiException("Mã giảm giá không tồn tại.", "INVALID_VOUCHER");
        }
        if (!Boolean.TRUE.equals(phieu.getIsActive())) {
            throw new ApiException("Mã giảm giá không còn hiệu lực.", "INVALID_VOUCHER");
        }
        if (phieu.getNgayBatDau() == null || phieu.getNgayKetThuc() == null) {
            throw new ApiException("Mã giảm giá chưa được cấu hình thời gian áp dụng.", "INVALID_VOUCHER");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(phieu.getNgayBatDau())) {
            throw new ApiException("Mã giảm giá chưa đến thời gian áp dụng.", "INVALID_VOUCHER");
        }
        if (now.isAfter(phieu.getNgayKetThuc())) {
            throw new ApiException("Mã giảm giá đã hết hạn.", "INVALID_VOUCHER");
        }
        if (phieu.getSoLuong() == null || phieu.getSoLuong() <= 0) {
            throw new ApiException("Mã giảm giá đã hết lượt sử dụng.", "INVALID_VOUCHER");
        }
        BigDecimal donToiThieu = phieu.getGiaTriDonToiThieu() != null
                ? phieu.getGiaTriDonToiThieu()
                : BigDecimal.ZERO;
        if (tongTien.compareTo(donToiThieu) < 0) {
            throw new ApiException(
                    "Đơn hàng chưa đạt giá trị tối thiểu " + donToiThieu.toPlainString() + "đ.",
                    "INVALID_VOUCHER");
        }
    }
}
