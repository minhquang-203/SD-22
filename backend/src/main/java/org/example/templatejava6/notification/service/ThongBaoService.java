package org.example.templatejava6.notification.service;

import org.example.templatejava6.notification.entity.ThongBao;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.example.templatejava6.notification.model.response.ThongBaoResponse;
import org.example.templatejava6.notification.model.response.ThongBaoTongQuanResponse;
import org.example.templatejava6.notification.repository.ThongBaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThongBaoService {

    private static final Logger log = LoggerFactory.getLogger(ThongBaoService.class);
    private static final int MAX_HIEN_THI = 30;

    private final ThongBaoRepository thongBaoRepository;

    public ThongBaoService(ThongBaoRepository thongBaoRepository) {
        this.thongBaoRepository = thongBaoRepository;
    }

    /** Tạo thông báo cho admin. Chạy giao dịch riêng + nuốt lỗi để không ảnh hưởng luồng nghiệp vụ chính. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void taoThongBao(LoaiThongBao loai, String tieuDe, String noiDung,
                            String link, Integer idThamChieu, String maThamChieu) {
        try {
            ThongBao tb = new ThongBao();
            tb.setLoai(loai);
            tb.setTieuDe(tieuDe);
            tb.setNoiDung(noiDung);
            tb.setLink(link);
            tb.setIdThamChieu(idThamChieu);
            tb.setMaThamChieu(maThamChieu);
            tb.setIdKhachHang(null); // thông báo cho admin/hệ thống
            tb.setDaDoc(false);
            tb.setThoiGian(LocalDateTime.now());
            thongBaoRepository.save(tb);
        } catch (Exception ex) {
            log.error("Không tạo được thông báo admin: {}", ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public ThongBaoTongQuanResponse tongQuan() {
        long soChuaDoc = thongBaoRepository.countByIdKhachHangIsNullAndDaDocFalse();
        List<ThongBaoResponse> danhSach = thongBaoRepository
                .findByIdKhachHangIsNullOrderByThoiGianDescIdDesc(PageRequest.of(0, MAX_HIEN_THI))
                .stream()
                .map(ThongBaoResponse::new)
                .toList();
        return new ThongBaoTongQuanResponse(soChuaDoc, danhSach);
    }

    @Transactional(readOnly = true)
    public long demChuaDoc() {
        return thongBaoRepository.countByIdKhachHangIsNullAndDaDocFalse();
    }

    @Transactional
    public void danhDauDaDocTatCa() {
        thongBaoRepository.markAllAdminRead();
    }

    @Transactional
    public void danhDauDaDoc(Integer id) {
        thongBaoRepository.findById(id).ifPresent(tb -> {
            if (!Boolean.TRUE.equals(tb.getDaDoc())) {
                tb.setDaDoc(true);
                thongBaoRepository.save(tb);
            }
        });
    }
}
