package org.example.templatejava6.quiz.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.templatejava6.quiz.model.response.KetQuaQuizAdminResponse;
import org.example.templatejava6.quiz.model.response.SanPhamGoiYResponse;
import org.example.templatejava6.quiz.repository.KetQuaQuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KetQuaQuizAdminService {

    @Autowired
    private KetQuaQuizRepository ketQuaQuizRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy toàn bộ kết quả Quiz kèm thông tin khách hàng (hoặc null nếu khách vãng lai).
     */
    @Transactional(readOnly = true)
    public List<KetQuaQuizAdminResponse> getAllKetQua() {
        return ketQuaQuizRepository.findAllWithDetails().stream()
                .map(kq -> KetQuaQuizAdminResponse.builder()
                        .id(kq.getId())
                        .tenKhachHang(kq.getKhachHang() != null ? kq.getKhachHang().getHoTen() : null)
                        .emailKhachHang(kq.getKhachHang() != null ? kq.getKhachHang().getEmail() : null)
                        .sdtKhachHang(kq.getKhachHang() != null ? kq.getKhachHang().getSoDienThoai() : null)
                        .idLoaiDa(kq.getLoaiDaKetQua().getId())
                        .maLoaiDa(kq.getLoaiDaKetQua().getMa())
                        .tenLoaiDa(kq.getLoaiDaKetQua().getTen())
                        .moTaLoaiDa(kq.getLoaiDaKetQua().getMoTa())
                        .thoiGian(kq.getThoiGian())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách sản phẩm gợi ý theo loại da (từ bảng san_pham_loai_da).
     * JOIN lấy ảnh chính + giá thấp nhất từ các biến thể.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<SanPhamGoiYResponse> getSanPhamGoiY(Integer idLoaiDa) {
        String sql = """
                SELECT sp.id, sp.ten,
                       (SELECT TOP 1 url FROM anh_san_pham WHERE id_san_pham = sp.id ORDER BY la_anh_chinh DESC, id ASC) AS anhChinhUrl,
                       (SELECT MIN(gia_ban) FROM chi_tiet_san_pham WHERE id_san_pham = sp.id AND trang_thai = 1) AS giaBan
                FROM san_pham_loai_da spld
                JOIN san_pham sp ON spld.id_san_pham = sp.id
                WHERE spld.id_loai_da = :idLoaiDa AND sp.trang_thai = 1
                """;
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("idLoaiDa", idLoaiDa)
                .getResultList();
        return rows.stream()
                .map(r -> SanPhamGoiYResponse.builder()
                        .id((Integer) r[0])
                        .ten((String) r[1])
                        .anhChinhUrl((String) r[2])
                        .giaBan(r[3] != null ? new BigDecimal(r[3].toString()) : null)
                        .build())
                .collect(Collectors.toList());
    }
}
