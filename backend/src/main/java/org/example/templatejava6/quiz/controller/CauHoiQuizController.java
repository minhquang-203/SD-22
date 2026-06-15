package org.example.templatejava6.quiz.controller;

import org.example.templatejava6.quiz.model.request.CauHoiQuizRequest;
import org.example.templatejava6.quiz.service.CauHoiQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quizzes")
@CrossOrigin("*")
public class CauHoiQuizController {

    @Autowired
    private CauHoiQuizService cauHoiQuizService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(cauHoiQuizService.getAllQuizzes());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CauHoiQuizRequest request) {
        return ResponseEntity.ok(cauHoiQuizService.createQuiz(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody CauHoiQuizRequest request) {
        return ResponseEntity.ok(cauHoiQuizService.updateQuiz(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        cauHoiQuizService.deleteQuiz(id);
        return ResponseEntity.ok("Đã xóa thành công");
    }
}
