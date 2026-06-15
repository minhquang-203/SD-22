package org.example.templatejava6.quiz.service;

import org.example.templatejava6.quiz.entity.CauHoiQuiz;
import org.example.templatejava6.quiz.entity.DapAnQuiz;
import org.example.templatejava6.common.entity.LoaiDa;
import org.example.templatejava6.quiz.model.request.CauHoiQuizRequest;
import org.example.templatejava6.quiz.model.response.CauHoiQuizResponse;
import org.example.templatejava6.quiz.repository.CauHoiQuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CauHoiQuizService {

    @Autowired
    private CauHoiQuizRepository cauHoiRepo;

    // LẤY DANH SÁCH & MAP SANG RESPONSE
    public List<CauHoiQuizResponse> getAllQuizzes() {
        List<CauHoiQuiz> entities = cauHoiRepo.findAll();
        return entities.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // THÊM MỚI
    @Transactional
    public CauHoiQuizResponse createQuiz(CauHoiQuizRequest request) {
        CauHoiQuiz cauHoi = new CauHoiQuiz();
        cauHoi.setNoiDung(request.getTitle());

        int soThuTuMoi = (int) cauHoiRepo.count() + 1;
        cauHoi.setThuTu(soThuTuMoi);

        List<DapAnQuiz> dapAnList = new ArrayList<>();
        for (CauHoiQuizRequest.DapAnRequest ansReq : request.getAnswers()) {
            DapAnQuiz dapAn = new DapAnQuiz();
            dapAn.setIcon(ansReq.getIcon());
            dapAn.setNoiDung(ansReq.getLabel());
            dapAn.setDiem(ansReq.getScoreValue());

            LoaiDa loaiDa = new LoaiDa();
            loaiDa.setId(ansReq.getTagId());
            dapAn.setLoaiDa(loaiDa);

            dapAn.setCauHoi(cauHoi);
            dapAnList.add(dapAn);
        }
        cauHoi.setDapAns(dapAnList);
        cauHoi = cauHoiRepo.save(cauHoi);
        return mapToResponse(cauHoi);
    }

    // CẬP NHẬT (SỬA)
    @Transactional
    public CauHoiQuizResponse updateQuiz(Integer id, CauHoiQuizRequest request) {
        CauHoiQuiz cauHoi = cauHoiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        cauHoi.setNoiDung(request.getTitle());

        // Xóa đáp án cũ (nhờ orphanRemoval = true ở Entity) và thay bằng đáp án mới
        cauHoi.getDapAns().clear();

        for (CauHoiQuizRequest.DapAnRequest ansReq : request.getAnswers()) {
            DapAnQuiz dapAn = new DapAnQuiz();
            dapAn.setIcon(ansReq.getIcon());
            dapAn.setNoiDung(ansReq.getLabel());
            dapAn.setDiem(ansReq.getScoreValue());

            LoaiDa loaiDa = new LoaiDa();
            loaiDa.setId(ansReq.getTagId());
            dapAn.setLoaiDa(loaiDa);

            dapAn.setCauHoi(cauHoi);
            cauHoi.getDapAns().add(dapAn);
        }

        cauHoi = cauHoiRepo.save(cauHoi);
        return mapToResponse(cauHoi);
    }

    // XÓA
    @Transactional
    public void deleteQuiz(Integer id) {
        cauHoiRepo.deleteById(id);
    }

    // HÀM HELPER: Map Entity -> Response
    private CauHoiQuizResponse mapToResponse(CauHoiQuiz entity) {
        CauHoiQuizResponse response = new CauHoiQuizResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getNoiDung());

        List<CauHoiQuizResponse.DapAnResponse> ansResponses = new ArrayList<>();
        if(entity.getDapAns() != null) {
            for (DapAnQuiz ans : entity.getDapAns()) {
                CauHoiQuizResponse.DapAnResponse ansRes = new CauHoiQuizResponse.DapAnResponse();
                ansRes.setIcon(ans.getIcon());
                ansRes.setLabel(ans.getNoiDung());
                ansRes.setScoreValue(ans.getDiem());
                ansRes.setTagId(ans.getLoaiDa() != null ? ans.getLoaiDa().getId() : null);
                ansResponses.add(ansRes);
            }
        }
        response.setAnswers(ansResponses);
        return response;
    }
}