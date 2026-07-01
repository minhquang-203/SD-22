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
import java.util.Collection;
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

    public boolean hasSaleItems(Collection<Integer> variantIds, Map<Integer, VariantSaleInfo> saleMap) {
        if (variantIds == null || saleMap == null || saleMap.isEmpty()) {
            return false;
        }
        for (Integer variantId : variantIds) {
            if (variantId != null && saleMap.containsKey(variantId)) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal tinhTienGiamPhieu(
            PhieuGiamGia phieu,
            BigDecimal tongTien,
            Collection<Integer> variantIds,
            Map<Integer, VariantSaleInfo> saleMap) {
        validatePhieu(phieu, tongTien, variantIds, saleMap);

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

    private void validatePhieu(
            PhieuGiamGia phieu,
            BigDecimal tongTien,
            Collection<Integer> variantIds,
            Map<Integer, VariantSaleInfo> saleMap) {
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
        if (hasSaleItems(variantIds, saleMap)) {
            throw new ApiException(
                    "Không thể áp dụng mã giảm giá khi đơn có sản phẩm đang trong đợt giảm giá.",
                    "VOUCHER_SALE_CONFLICT");
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
