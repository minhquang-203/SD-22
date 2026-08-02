package org.example.templatejava6.banner.controller;

import org.example.templatejava6.banner.model.response.BannerTrangChuResponse;
import org.example.templatejava6.banner.service.BannerTrangChuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/khach/banners")
@CrossOrigin("*")
public class BannerPublicController {

    @Autowired
    private BannerTrangChuService bannerTrangChuService;

    @GetMapping
    public ResponseEntity<List<BannerTrangChuResponse>> listActive() {
        return ResponseEntity.ok(bannerTrangChuService.listActive());
    }
}
