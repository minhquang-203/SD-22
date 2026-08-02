package org.example.templatejava6.nhaphang.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.nhaphang.model.request.NhaCungCapRequest;
import org.example.templatejava6.nhaphang.model.response.NhaCungCapResponse;
import org.example.templatejava6.nhaphang.service.NhaCungCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/nha-cung-cap")
public class NhaCungCapController {

    @Autowired private NhaCungCapService nhaCungCapService;

    @GetMapping
    public List<NhaCungCapResponse> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "activeOnly", required = false, defaultValue = "true") boolean activeOnly) {
        return nhaCungCapService.list(q, activeOnly);
    }

    @GetMapping("{id}")
    public NhaCungCapResponse detail(@PathVariable Integer id) {
        return nhaCungCapService.detail(id);
    }

    @PostMapping
    public NhaCungCapResponse create(@Valid @RequestBody NhaCungCapRequest request) {
        return nhaCungCapService.create(request);
    }

    @PutMapping("{id}")
    public NhaCungCapResponse update(@PathVariable Integer id, @Valid @RequestBody NhaCungCapRequest request) {
        return nhaCungCapService.update(id, request);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Integer id) {
        nhaCungCapService.softDelete(id);
    }
}
