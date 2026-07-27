package org.example.templatejava6.quiz.service;

import org.example.templatejava6.common.entity.LoaiDa;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.repository.LoaiDaRepository;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.quiz.entity.KetQuaQuiz;
import org.example.templatejava6.quiz.model.request.KetQuaQuizRequest;
import org.example.templatejava6.quiz.repository.KetQuaQuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KetQuaQuizService {

    @Autowired
    private KetQuaQuizRepository ketQuaQuizRepository;

    @Autowired
    private LoaiDaRepository loaiDaRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Transactional
    public void luuKetQua(KetQuaQuizRequest request) {
        if (request.getIdLoaiDa() == null) {
            throw new ApiException("Thiếu loại da kết quả", "VALIDATION_ERROR");
        }
        LoaiDa loaiDa = loaiDaRepository.findById(request.getIdLoaiDa())
                .orElseThrow(() -> new ApiException("Không tìm thấy loại da", "NOT_FOUND"));

        KetQuaQuiz ketQua = new KetQuaQuiz();
        ketQua.setLoaiDaKetQua(loaiDa);

        // Gắn khách hàng nếu đang đăng nhập, bỏ qua nếu là khách vãng lai
        Integer khachHangId = getCurrentKhachHangIdOrNull();
        if (khachHangId != null) {
            khachHangRepository.findById(khachHangId).ifPresent(ketQua::setKhachHang);
        }

        ketQuaQuizRepository.save(ketQua);
    }

    public org.example.templatejava6.quiz.model.response.KetQuaQuizToiResponse layKetQuaQuizCuaToi() {
        Integer khachHangId = getCurrentKhachHangIdOrNull();
        if (khachHangId == null) {
            throw new ApiException("Vui lòng đăng nhập", "UNAUTHORIZED");
        }
        return ketQuaQuizRepository.findFirstByKhachHang_IdOrderByThoiGianDesc(khachHangId)
                .map(kq -> {
                    org.example.templatejava6.quiz.model.response.KetQuaQuizToiResponse res = new org.example.templatejava6.quiz.model.response.KetQuaQuizToiResponse();
                    res.setId(kq.getId());
                    res.setIdLoaiDa(kq.getLoaiDaKetQua().getId());
                    res.setTenLoaiDa(kq.getLoaiDaKetQua().getTen());
                    res.setMoTaLoaiDa(kq.getLoaiDaKetQua().getMoTa());
                    res.setThoiGianLam(kq.getThoiGian());
                    return res;
                })
                .orElse(null);
    }

    /**
     * Lấy ID khách hàng từ JWT nếu có, trả về null nếu khách chưa đăng nhập (vãng lai).
     */
    private Integer getCurrentKhachHangIdOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
                return null;
            }
            return Integer.valueOf(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
