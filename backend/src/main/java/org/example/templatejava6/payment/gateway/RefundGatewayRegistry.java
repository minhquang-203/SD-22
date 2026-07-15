package org.example.templatejava6.payment.gateway;

import org.example.templatejava6.common.exception.ApiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RefundGatewayRegistry {

    private final Map<String, RefundGateway> gateways;

    public RefundGatewayRegistry(List<RefundGateway> gateways) {
        this.gateways = gateways.stream()
                .collect(Collectors.toMap(
                        gateway -> normalize(gateway.getProviderCode()),
                        Function.identity()
                ));
    }

    public RefundGateway getGateway(String providerCode) {
        return getGatewayOptional(providerCode)
                .orElseThrow(() -> new ApiException(
                        "Cổng hoàn tiền không được hỗ trợ.", "UNSUPPORTED_REFUND_PROVIDER"));
    }

    /**
     * Tra Optional empty neu provider chua co gateway auto-refund (vd: CHUYEN_KHOAN)
     * → cho phep fallback thu cong.
     */
    public Optional<RefundGateway> getGatewayOptional(String providerCode) {
        return Optional.ofNullable(gateways.get(normalize(providerCode)));
    }

    private String normalize(String providerCode) {
        return providerCode == null ? "" : providerCode.trim().toUpperCase(Locale.ROOT);
    }
}
