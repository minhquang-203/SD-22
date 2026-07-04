package org.example.templatejava6.review.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.service.SanPhamService;
import org.example.templatejava6.review.entity.DanhGia;
import org.example.templatejava6.review.model.request.DanhGiaRequest;
import org.example.templatejava6.review.model.response.DanhGiaResponse;
import org.example.templatejava6.review.repository.DanhGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import org.example.templatejava6.common.service.ProductFileStorageService;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.example.templatejava6.order.entity.HoaDonChiTiet;

@Service
public class DanhGiaService {

    @Autowired private DanhGiaRepository danhGiaRepository;
    @Autowired private SanPhamService sanPhamService;
    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired private ProductFileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getApprovedBySanPham(Integer idSanPham) {
        SanPham sp = sanPhamService.getSanPhamOrThrow(idSanPham);
        return danhGiaRepository.findBySanPhamAndTrangThai(sp, "DA_DUYET")
                .stream().map(DanhGiaResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getChoDuyet() {
        return danhGiaRepository.findByTrangThai("CHO_DUYET")
                .stream().map(DanhGiaResponse::new).toList();
    }

    @Transactional
    public void add(DanhGiaRequest request, MultipartFile file) {
        if (request.getIdHoaDonChiTiet() != null
                && danhGiaRepository.findFirstByHoaDonChiTiet_Id(request.getIdHoaDonChiTiet()).isPresent()) {
            throw new ApiException("Bạn đã đánh giá sản phẩm này trong đơn hàng", "VALIDATION_ERROR");
        }

        DanhGia dg = new DanhGia();
        dg.setIdKhachHang(request.getIdKhachHang());
        dg.setSoSao(request.getSoSao());
        dg.setNoiDung(request.getNoiDung());
        
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

        if (request.getIdSanPham() != null) {
            dg.setSanPham(sanPhamService.getSanPhamOrThrow(request.getIdSanPham()));
        } else {
            throw new ApiException("Lỗi: idSanPham không được trống. Dữ liệu cũ có thể bị thiếu.", "VALIDATION_ERROR");
        }
        
        if (request.getIdHoaDonChiTiet() != null) {
            HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(request.getIdHoaDonChiTiet())
                    .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));
            dg.setHoaDonChiTiet(hdct);
        }
        
        dg.setTrangThai("CHO_DUYET");
        dg.setNgayTao(LocalDateTime.now());
        danhGiaRepository.save(dg);
    }

    @Transactional
    public void duyet(Integer id, String trangThai) {
        if (!"DA_DUYET".equals(trangThai) && !"TU_CHOI".equals(trangThai)) {
            throw new ApiException("Trạng thái duyệt chỉ được DA_DUYET hoặc TU_CHOI", "VALIDATION_ERROR");
        }
        DanhGia dg = danhGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá", "NOT_FOUND"));
        dg.setTrangThai(trangThai);
        danhGiaRepository.save(dg);
    }

    @Transactional(readOnly = true)
    public List<DanhGiaResponse> getAllReviews() {
        return danhGiaRepository.findAll()
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
    public void likeDanhGia(Integer id) {
        DanhGia dg = danhGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá", "NOT_FOUND"));
        dg.setSoLuotThich((dg.getSoLuotThich() != null ? dg.getSoLuotThich() : 0) + 1);
        danhGiaRepository.save(dg);
    }
}
