package org.example.templatejava6.shipping.controller;

import jakarta.validation.Valid;
import org.example.templatejava6.shipping.model.request.CreateShippingOrderRequest;
import org.example.templatejava6.shipping.model.request.ShippingFeeRequest;
import org.example.templatejava6.shipping.model.response.CreateShippingOrderResponse;
import org.example.templatejava6.shipping.model.response.GhnProvinceResponse;
import org.example.templatejava6.shipping.model.response.GhnWardResponse;
import org.example.templatejava6.shipping.model.response.ShippingFeeResponse;
import org.example.templatejava6.shipping.service.ShippingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /** Danh sách tỉnh/thành đơn vị hành chính mới (GHN v3). */
    @GetMapping("/provinces")
    public List<GhnProvinceResponse> provinces() {
        return shippingService.getProvinces();
    }

    /** Danh sách phường/xã theo tỉnh (GHN v3, 2 cấp). */
    @GetMapping("/wards")
    public List<GhnWardResponse> wards(@RequestParam Integer provinceId) {
        return shippingService.getWardsByProvince(provinceId);
    }

    @PostMapping("/fee")
    public ShippingFeeResponse fee(@Valid @RequestBody ShippingFeeRequest request) {
        return shippingService.calcFee(request);
    }

    @PostMapping("/orders")
    public CreateShippingOrderResponse createOrder(@Valid @RequestBody CreateShippingOrderRequest request) {
        return shippingService.createOrder(request);
    }
}
