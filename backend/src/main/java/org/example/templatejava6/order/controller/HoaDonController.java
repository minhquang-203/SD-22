package org.example.templatejava6.order.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.order.model.request.HoaDonChuyenTrangThaiRequest;
import org.example.templatejava6.order.model.request.HoaDonGhnWebhookRequest;
import org.example.templatejava6.order.model.request.HoaDonRequest;
import org.example.templatejava6.order.model.request.HoaDonTuChoiRequest;
import org.example.templatejava6.order.model.response.GhnTrangThaiOptionResponse;
import org.example.templatejava6.order.model.response.HoaDonDetailResponse;
import org.example.templatejava6.order.model.response.HoaDonResponse;
import org.example.templatejava6.order.model.response.StorefrontOrderDetailResponse;
import org.example.templatejava6.order.model.response.StorefrontOrderSummaryResponse;
import org.example.templatejava6.order.service.GhnOrderCreationService;
import org.example.templatejava6.order.service.GhnOrderSyncService;
import org.example.templatejava6.order.service.GhnTrackingService;
import org.example.templatejava6.order.service.HoaDonService;
import org.example.templatejava6.order.service.HoaDonStorefrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonStorefrontService hoaDonStorefrontService;

    @Autowired
    private GhnOrderSyncService ghnOrderSyncService;

    @Autowired
    private GhnOrderCreationService ghnOrderCreationService;

    @GetMapping("/cua-toi")
    public List<StorefrontOrderSummaryResponse> donCuaToi() {
        return hoaDonStorefrontService.donCuaToi();
    }

    @GetMapping("/cua-toi/{id}")
    public StorefrontOrderDetailResponse chiTietCuaToi(@PathVariable Integer id) {
        return hoaDonStorefrontService.chiTietCuaToi(id);
    }

    @PostMapping("/cua-toi/{id}/huy")
    public StorefrontOrderDetailResponse huyDonCuaToi(
            @PathVariable Integer id,
            @RequestBody(required = false) HoaDonTuChoiRequest request) {
        return hoaDonStorefrontService.huyDonCuaToi(
                id,
                request != null ? request.getGhiChu() : null);
    }

    @GetMapping
    public List<HoaDonResponse> getAll() {
        return hoaDonService.getAll();
    }

    @GetMapping("/paging")
    public List<HoaDonResponse> paging(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize
    ) {
        return hoaDonService.phanTrang(pageNo, pageSize).getContent();
    }

    @GetMapping("/{id}")
    public HoaDonDetailResponse detail(@PathVariable Integer id) {
        return hoaDonService.detail(id);
    }

    @GetMapping("/search")
    public List<HoaDonResponse> search(
            @RequestParam("keyword") String keyword
    ) {
        return hoaDonService.timKiem(keyword);
    }

    @PostMapping
    public void create(
            @Valid @RequestBody HoaDonRequest request
    ) {
        hoaDonService.add(request);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable Integer id,
            @Valid @RequestBody HoaDonRequest request
    ) {
        hoaDonService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public void changeStatus(
            @PathVariable Integer id,
            @Valid @RequestBody HoaDonChuyenTrangThaiRequest request
    ) {
        hoaDonService.chuyenTrangThai(
                id,
                request.getTrangThai(),
                request.getGhiChu(),
                request.getIdNhanVien()
        );
    }

    @PostMapping("/{id}/tu-choi")
    public void tuChoiDon(
            @PathVariable Integer id,
            @RequestBody(required = false) HoaDonTuChoiRequest request
    ) {
        hoaDonService.tuChoiDon(
                id,
                request != null ? request.getGhiChu() : null,
                request != null ? request.getIdNhanVien() : null
        );
    }
    
    @PostMapping("/{id}/dong-bo-ghn")
    public GhnOrderSyncService.KetQuaDongBo dongBoGhn(@PathVariable Integer id) {
        return ghnOrderSyncService.dongBoTheoId(id);
    }

    @GetMapping("/ghn-trang-thai")
    public List<GhnTrangThaiOptionResponse> ghnTrangThai() {
        return GhnTrackingService.allStatusOptions();
    }

    @PostMapping("/{id}/gia-lap-webhook-ghn")
    public GhnOrderSyncService.KetQuaDongBo giaLapWebhookGhn(
            @PathVariable Integer id,
            @Valid @RequestBody HoaDonGhnWebhookRequest request
    ) {
        return ghnOrderSyncService.giaLapWebhookTheoId(
                id,
                request.getStatus(),
                request.getGhiChu());
    }

    @PostMapping("/{id}/tao-van-don-ghn")
    public GhnOrderCreationService.KetQua taoVanDonGhn(@PathVariable Integer id) {
        return ghnOrderCreationService.taoVanDonTheoId(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        hoaDonService.delete(id);
    }
}