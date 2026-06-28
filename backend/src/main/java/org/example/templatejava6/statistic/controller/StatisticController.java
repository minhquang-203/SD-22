package org.example.templatejava6.statistic.controller;

import org.example.templatejava6.statistic.model.response.TongQuanResponse;
import org.example.templatejava6.statistic.repository.StatisticRepository;
import org.example.templatejava6.statistic.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/statistic")
@CrossOrigin("*")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/summary")
    public ResponseEntity<TongQuanResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        return ResponseEntity.ok(statisticService.getTongQuan(fromDate, toDate));
    }

    @Autowired
    private StatisticRepository statisticRepository; // Khai báo thêm cái này nếu chưa có

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(statisticRepository.getTopProducts(fromDate, toDate));
    }

    @GetMapping("/payment-flow")
    public ResponseEntity<?> getPaymentFlow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(statisticRepository.getPaymentFlow(fromDate, toDate));
    }

    @GetMapping("/chart")
    public ResponseEntity<?> getChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(statisticRepository.getChartData(fromDate, toDate));
    }
    @GetMapping("/voucher")
    public ResponseEntity<?> getVoucherStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(statisticRepository.getVoucherStats(fromDate, toDate));
    }

    @GetMapping("/quiz")
    public ResponseEntity<?> getQuizStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(statisticRepository.getQuizStats(fromDate, toDate));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStock() {
        return ResponseEntity.ok(statisticRepository.getLowStockProducts());
    }
}