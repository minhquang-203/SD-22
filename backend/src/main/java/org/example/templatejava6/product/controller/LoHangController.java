package org.example.templatejava6.product.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.product.model.request.LoHangRequest;
import org.example.templatejava6.product.model.response.LoHangResponse;
import org.example.templatejava6.product.service.LoHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/lo-hang")
public class LoHangController {

    @Autowired
    private LoHangService loHangService;

    @GetMapping("chi-tiet-san-pham/{idCts}")
    public List<LoHangResponse> listByChiTiet(@PathVariable("idCts") Integer idCts) {
        return loHangService.listByChiTiet(idCts);
    }

    @PostMapping
    public LoHangResponse nhapLo(@Valid @RequestBody LoHangRequest request) {
        return loHangService.nhapLo(request);
    }
}
