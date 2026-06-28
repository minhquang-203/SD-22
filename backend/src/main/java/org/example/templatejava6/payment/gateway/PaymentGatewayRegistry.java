package org.example.templatejava6.payment.gateway;

import org.example.templatejava6.common.exception.ApiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayRegistry {

    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> gateways) {
        this.gateways = gateways.stream()
                .collect(Collectors.toMap(
                        gateway -> normalize(gateway.getProviderCode()),
                        Function.identity()
                ));
    }

    public PaymentGateway getGateway(String providerCode) {
        PaymentGateway gateway = gateways.get(normalize(providerCode));
        if (gateway == null) {
            throw new ApiException("Cổng thanh toán không được hỗ trợ.", "UNSUPPORTED_PAYMENT_PROVIDER");
        }
        return gateway;
    }

    private String normalize(String providerCode) {
        return providerCode == null ? "" : providerCode.trim().toUpperCase(Locale.ROOT);
    }
}
