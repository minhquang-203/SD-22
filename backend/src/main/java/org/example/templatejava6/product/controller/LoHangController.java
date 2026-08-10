package org.example.templatejava6.product.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.common.exception.ApiException;
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

    /** POS: lô còn hàng của biến thể, sort HSD tăng dần. */
    @GetMapping("theo-bien-the/{idCts}")
    public List<LoHangResponse> listConHangTheoBienThe(@PathVariable("idCts") Integer idCts) {
        return loHangService.listConHangTheoBienThe(idCts);
    }

    @PostMapping
    public LoHangResponse nhapLo(@Valid @RequestBody LoHangRequest request) {
        throw new ApiException(
                "Lô chỉ được tạo qua phiếu nhập hàng. Vào menu Nhập hàng để lập phiếu.",
                "FORBIDDEN");
    }

    @PutMapping("{id}")
    public LoHangResponse capNhatLo(@PathVariable("id") Integer id,
                                    @Valid @RequestBody LoHangRequest request) {
        return loHangService.capNhatLo(id, request);
    }

    @DeleteMapping("{id}")
    public void xoaLo(@PathVariable("id") Integer id) {
        loHangService.xoaLo(id);
    }
}
