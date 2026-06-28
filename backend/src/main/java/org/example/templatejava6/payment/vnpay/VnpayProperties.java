package org.example.templatejava6.payment.vnpay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.vnpay")
public class VnpayProperties {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String frontendReturnUrl;
    private String version = "2.1.0";
    private String command = "pay";
    private String currCode = "VND";
    private String locale = "vn";
    private String orderType = "other";
}
