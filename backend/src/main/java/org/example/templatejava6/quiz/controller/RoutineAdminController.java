package org.example.templatejava6.quiz.controller;

import org.example.templatejava6.quiz.model.request.RoutineComboRequest;
import org.example.templatejava6.quiz.model.response.RoutineComboResponse;
import org.example.templatejava6.quiz.service.RoutineComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/routines")
@CrossOrigin("*")
public class RoutineAdminController {

    @Autowired
    private RoutineComboService routineComboService;

    @GetMapping
    public ResponseEntity<List<RoutineComboResponse>> getAll() {
        return ResponseEntity.ok(routineComboService.getAllRoutines());
    }

    @PostMapping
    public ResponseEntity<RoutineComboResponse> create(@RequestBody RoutineComboRequest request) {
        return ResponseEntity.ok(routineComboService.createRoutine(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineComboResponse> update(@PathVariable Integer id, @RequestBody RoutineComboRequest request) {
        return ResponseEntity.ok(routineComboService.updateRoutine(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        routineComboService.deleteRoutine(id);
        return ResponseEntity.ok().build();
    }
}
