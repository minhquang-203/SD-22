package org.example.templatejava6.review.service;

import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.entity.HoaDonChiTiet;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.service.SanPhamService;
import org.example.templatejava6.review.entity.DanhGia;
import org.example.templatejava6.review.model.request.DanhGiaRequest;
import org.example.templatejava6.review.model.response.DanhGiaResponse;
import org.example.templatejava6.review.repository.DanhGiaRepository;
import org.example.templatejava6.common.service.ProductFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DanhGiaService {

    @Autowired private DanhGiaRepository danhGiaRepository;
    @Autowired private SanPhamService sanPhamService;
    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired private ProductFileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getBySanPham(Integer idSanPham) {
        SanPham sp = sanPhamService.getSanPhamOrThrow(idSanPham);
        return danhGiaRepository.findBySanPhamOrderByNgayTaoDesc(sp)
                .stream().map(DanhGiaResponse::new).toList();
    }

    @Transactional
    public void add(DanhGiaRequest request, MultipartFile file) {
        if (request.getIdHoaDonChiTiet() == null) {
            throw new ApiException("Chỉ đánh giá được sản phẩm trong đơn đã giao.", "VALIDATION_ERROR");
        }
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(request.getIdHoaDonChiTiet())
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm trong đơn hàng.", "VALIDATION_ERROR"));
        HoaDon hoaDon = hdct.getIdHoaDon();
        if (!soHuuDonDeDanhGia(hoaDon, request.getTrackingToken())) {
            throw new ApiException("Bạn chỉ đánh giá được sản phẩm trong đơn đã giao của mình.", "VALIDATION_ERROR");
        }
        if (danhGiaRepository.findFirstByHoaDonChiTiet_Id(request.getIdHoaDonChiTiet()).isPresent()) {
            throw new ApiException("Bạn đã đánh giá sản phẩm này trong đơn hàng", "VALIDATION_ERROR");
        }
        if (hoaDon.getTrangThai() != TrangThaiDonHang.HOAN_THANH) {
            throw new ApiException("Chỉ đánh giá được khi đơn đã giao.", "VALIDATION_ERROR");
        }
        Integer idSanPham = idSanPhamCuaDong(hdct);
        if (idSanPham == null || !idSanPham.equals(request.getIdSanPham())) {
            throw new ApiException("Sản phẩm không thuộc dòng đơn này.", "VALIDATION_ERROR");
        }

        DanhGia dg = new DanhGia();
        dg.setIdKhachHang(hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null);
        dg.setSoSao(request.getSoSao());
        dg.setNoiDung(request.getNoiDung());
        dg.setHoaDonChiTiet(hdct);
        
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.store(file);
            dg.setHinhAnhVideo("/uploads/products/" + fileName);
        } else if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            try {
                String base64Data = request.getImageBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Data);
                
                String fileName = java.util.UUID.randomUUID().toString() + "_review.png";
                java.nio.file.Path dir = java.nio.file.Paths.get("uploads/products").toAbsolutePath().normalize();
                java.nio.file.Files.createDirectories(dir);
                java.nio.file.Files.write(dir.resolve(fileName), decodedBytes);
                
                dg.setHinhAnhVideo("/uploads/products/" + fileName);
            } catch (Exception e) {
                dg.setHinhAnhVideo(null);
            }
        } else {
            dg.setHinhAnhVideo(request.getHinhAnhVideo());
        }

        dg.setSanPham(sanPhamService.getSanPhamOrThrow(idSanPham));

        dg.setTrangThai("DA_DUYET");
        dg.setNgayTao(LocalDateTime.now());
        danhGiaRepository.save(dg);
    }

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getAllReviews() {
        return danhGiaRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"))
                .stream().map(DanhGiaResponse::new).toList();
    }

    @Transactional
    public void phanHoiDanhGia(Integer id, String phanHoi) {
        DanhGia dg = danhGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá", "NOT_FOUND"));
        dg.setPhanHoiCuaShop(phanHoi);
        danhGiaRepository.save(dg);
    }

    @Transactional
    public void xoa(Integer id) {
        DanhGia dg = danhGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá", "NOT_FOUND"));
        danhGiaRepository.delete(dg);
    }

    @Transactional
    public void likeDanhGia(Integer id) {
        DanhGia dg = danhGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá", "NOT_FOUND"));
        dg.setSoLuotThich((dg.getSoLuotThich() != null ? dg.getSoLuotThich() : 0) + 1);
        danhGiaRepository.save(dg);
    }

    /** Khách đăng nhập khớp tài khoản trên đơn, hoặc khách vãng lai khớp mã tra cứu. */
    private boolean soHuuDonDeDanhGia(HoaDon hoaDon, String trackingToken) {
        if (hoaDon == null) {
            return false;
        }
        Integer idTrenDon = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;
        Integer idDangNhap = currentKhachHangIdOrNull();
        if (idTrenDon != null && idTrenDon.equals(idDangNhap)) {
            return true;
        }
        return tokenKhopDon(hoaDon, trackingToken);
    }

    private Integer currentKhachHangIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        boolean laKhach = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_KHACH_HANG".equals(authority.getAuthority()));
        if (!laKhach || authentication.getName() == null) {
            return null;
        }
        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean tokenKhopDon(HoaDon hoaDon, String token) {
        String expected = hoaDon.getTrackingToken();
        String given = token != null ? token.trim() : "";
        if (expected == null || expected.isBlank() || given.length() < 32) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = given.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private static Integer idSanPhamCuaDong(HoaDonChiTiet hdct) {
        if (hdct.getIdChiTietSanPham() == null || hdct.getIdChiTietSanPham().getSanPham() == null) {
            return null;
        }
        return hdct.getIdChiTietSanPham().getSanPham().getId();
    }
}
