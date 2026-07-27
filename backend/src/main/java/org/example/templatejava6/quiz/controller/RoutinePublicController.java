package org.example.templatejava6.quiz.controller;

import org.example.templatejava6.quiz.model.response.RoutineComboResponse;
import org.example.templatejava6.quiz.service.RoutineComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khach/routines")
@CrossOrigin("*")
public class RoutinePublicController {

    @Autowired
    private RoutineComboService routineComboService;

    @GetMapping("/loai-da/{id}")
    public ResponseEntity<List<RoutineComboResponse>> getByLoaiDa(@PathVariable Integer id) {
        return ResponseEntity.ok(routineComboService.getRoutinesByLoaiDa(id));
    }
}
