package org.example.templatejava6.quiz.controller;

import org.example.templatejava6.quiz.entity.CauHoiQuiz;
import org.example.templatejava6.quiz.entity.DapAnQuiz;
import org.example.templatejava6.quiz.model.response.CauHoiQuizResponse;
import org.example.templatejava6.quiz.repository.CauHoiQuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/khach/quiz")
@CrossOrigin("*")
public class QuizPublicController {

    @Autowired
    private CauHoiQuizRepository cauHoiQuizRepository;

    @GetMapping
    public ResponseEntity<?> getActiveQuizzes() {
        List<CauHoiQuiz> entities = cauHoiQuizRepository.findByTrangThaiTrueOrderByThuTuAsc();
        List<CauHoiQuizResponse> responses = entities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private CauHoiQuizResponse mapToResponse(CauHoiQuiz entity) {
        CauHoiQuizResponse response = new CauHoiQuizResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getNoiDung());

        List<CauHoiQuizResponse.DapAnResponse> ansResponses = new ArrayList<>();
        if (entity.getDapAns() != null) {
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
