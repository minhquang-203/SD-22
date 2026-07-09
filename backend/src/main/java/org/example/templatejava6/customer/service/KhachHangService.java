package org.example.templatejava6.customer.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.model.VaiTro;
import org.example.templatejava6.common.repository.VaiTroRepository;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.customer.model.request.TaoKhachNhanhRequest;
import org.example.templatejava6.customer.model.response.DiaChiResponse;
import org.example.templatejava6.customer.model.response.KhachHangDetailResponse;
import org.example.templatejava6.customer.model.response.KhachHangResponse;
import org.example.templatejava6.customer.repository.DiaChiKhachHangRepository;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private DiaChiKhachHangRepository diaChiKhachHangRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    private static final String MA_VAI_TRO_KHACH = "KHACH_HANG";
    private static final String MAT_KHAU_MAC_DINH = "pos@sunova";

    @Transactional(readOnly = true)
    public List<KhachHangResponse> getAll() {
        return khachHangRepository.findAll().stream().map(KhachHangResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<KhachHangResponse> phanTrang(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return khachHangRepository.findAll(pageable).map(KhachHangResponse::new);
    }

    @Transactional(readOnly = true)
    public List<KhachHangResponse> timKiem(String keyword) {
        return khachHangRepository.timKiem(keyword).stream().map(KhachHangResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public KhachHangDetailResponse detail(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));
        KhachHangDetailResponse response = new KhachHangDetailResponse(kh);
        response.setDiaChis(diaChiKhachHangRepository.findByKhachHangOrderByMacDinhDescIdDesc(kh).stream()
                .map(DiaChiResponse::new)
                .toList());
        return response;
    }

    @Transactional
    public void updateTrangThai(Integer id, Boolean trangThai) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));
        kh.setTrangThai(trangThai);
        khachHangRepository.save(kh);
    }

    @Transactional(readOnly = true)
    public KhachHangResponse timTheoSoDienThoai(String sdt) {
        if (sdt == null || sdt.isBlank()) {
            throw new ApiException("Vui lòng nhập số điện thoại.", "VALIDATION_ERROR");
        }
        return khachHangRepository.findBySoDienThoai(sdt.trim())
                .map(KhachHangResponse::new)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với SĐT này.", "NOT_FOUND"));
    }

    @Transactional
    public KhachHangResponse taoNhanh(TaoKhachNhanhRequest req) {
        if (req.getSoDienThoai() == null || req.getSoDienThoai().isBlank()) {
            throw new ApiException("Vui lòng nhập số điện thoại.", "VALIDATION_ERROR");
        }
        String sdt = req.getSoDienThoai().trim();
        return khachHangRepository.findBySoDienThoai(sdt)
                .map(KhachHangResponse::new)
                .orElseGet(() -> new KhachHangResponse(taoKhachMoi(req, sdt)));
    }

    private KhachHang taoKhachMoi(TaoKhachNhanhRequest req, String sdt) {
        VaiTro vaiTro = vaiTroRepository.findByMaVaiTro(MA_VAI_TRO_KHACH)
                .orElseThrow(() -> new ApiException("Vai trò khách hàng chưa được cấu hình.", "CONFIG_ERROR"));

        String hoTen = req.getHoTen() != null && !req.getHoTen().isBlank()
                ? req.getHoTen().trim() : "Khách " + sdt;

        KhachHang kh = new KhachHang();
        kh.setVaiTro(vaiTro);
        kh.setMaKhachHang(sinhMaKhachHang());
        kh.setHoTen(hoTen);
        kh.setSoDienThoai(sdt);
        kh.setEmail(sdt + "@pos.sunova.local");
        kh.setMatKhau(MAT_KHAU_MAC_DINH);
        kh.setGioiTinh("Khac");
        kh.setDiemTichLuy(0);
        kh.setTrangThai(true);
        kh.setNgayTao(LocalDateTime.now());
        return khachHangRepository.save(kh);
    }

    private String sinhMaKhachHang() {
        for (int i = 0; i < 50; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(100, 10000);
            String ma = "KH" + suffix;
            if (!khachHangRepository.existsByMaKhachHang(ma)) {
                return ma;
            }
        }
        throw new ApiException("Không thể sinh mã khách hàng. Vui lòng thử lại.", "CODE_GEN_FAILED");
    }
}
