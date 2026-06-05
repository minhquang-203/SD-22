package org.example.templatejava6.wishlist.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.wishlist.model.request.YeuThichRequest;
import org.example.templatejava6.wishlist.model.response.YeuThichResponse;
import org.example.templatejava6.wishlist.service.YeuThichService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/yeu-thich")
public class YeuThichController {

    @Autowired
    private YeuThichService yeuThichService;

    @GetMapping
    public List<YeuThichResponse> getByKhachHang(@RequestParam("idKhachHang") Integer idKhachHang) {
        return yeuThichService.getByKhachHang(idKhachHang);
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody YeuThichRequest request) {
        yeuThichService.add(request);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        yeuThichService.delete(id);
    }
}
