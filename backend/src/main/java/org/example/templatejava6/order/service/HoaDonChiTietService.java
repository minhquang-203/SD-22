package org.example.templatejava6.order.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.model.request.HoaDonChiTietRequest;
import org.example.templatejava6.order.model.response.HoaDonChiTietResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoaDonChiTietService {

    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired private HoaDonService hoaDonService;

    @Transactional
    public void add(HoaDonChiTietRequest request) {
        if (request.getIdHoaDon() == null) {
            throw new ApiException("Id hóa đơn không được để trống", "VALIDATION_ERROR");
        }
        HoaDon hoaDon = hoaDonService.getHoaDonOrThrow(request.getIdHoaDon());
        HoaDonChiTiet ct = hoaDonService.buildChiTiet(hoaDon, request);
        hoaDonChiTietRepository.save(ct);
        hoaDonService.recalculateTotals(hoaDon);
    }

    @Transactional
    public void update(Integer id, HoaDonChiTietRequest request) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));

        if (request.getIdChiTietSanPham() != null) {
            ctReqToEntity(ct, request);
        } else {
            if (request.getSoLuong() != null) {
                ct.setSoLuong(request.getSoLuong());
            }
            if (request.getDonGia() != null) {
                ct.setDonGia(request.getDonGia());
            }
        }
        ct.setThanhTien(ct.getDonGia().multiply(java.math.BigDecimal.valueOf(ct.getSoLuong())));
        ct.setId(id);
        hoaDonChiTietRepository.save(ct);
        hoaDonService.recalculateTotals(ct.getIdHoaDon());
    }

    @Transactional
    public void delete(Integer id) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));
        HoaDon hoaDon = ct.getIdHoaDon();
        hoaDonChiTietRepository.delete(ct);
        hoaDonService.recalculateTotals(hoaDon);
    }

    @Transactional(readOnly = true)
    public HoaDonChiTietResponse detail(Integer id) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));
        return new HoaDonChiTietResponse(ct);
    }

    private void ctReqToEntity(HoaDonChiTiet ct, HoaDonChiTietRequest request) {
        HoaDonChiTiet rebuilt = hoaDonService.buildChiTiet(ct.getIdHoaDon(), request);
        ct.setIdChiTietSanPham(rebuilt.getIdChiTietSanPham());
        ct.setSoLuong(rebuilt.getSoLuong());
        ct.setDonGia(rebuilt.getDonGia());
        ct.setThanhTien(rebuilt.getThanhTien());
    }
}
