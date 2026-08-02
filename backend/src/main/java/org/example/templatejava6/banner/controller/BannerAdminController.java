package org.example.templatejava6.banner.controller;

import org.example.templatejava6.banner.model.request.BannerTrangChuRequest;
import org.example.templatejava6.banner.model.response.BannerTrangChuResponse;
import org.example.templatejava6.banner.service.BannerTrangChuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/banners")
@CrossOrigin("*")
public class BannerAdminController {

    @Autowired
    private BannerTrangChuService bannerTrangChuService;

    @GetMapping
    public ResponseEntity<List<BannerTrangChuResponse>> listAll() {
        return ResponseEntity.ok(bannerTrangChuService.listAll());
    }

    @PostMapping
    public ResponseEntity<BannerTrangChuResponse> create(@RequestBody BannerTrangChuRequest request) {
        return ResponseEntity.ok(bannerTrangChuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BannerTrangChuResponse> update(@PathVariable Integer id,
                                                         @RequestBody BannerTrangChuRequest request) {
        return ResponseEntity.ok(bannerTrangChuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bannerTrangChuService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestPart("file") MultipartFile file) {
        String url = bannerTrangChuService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
