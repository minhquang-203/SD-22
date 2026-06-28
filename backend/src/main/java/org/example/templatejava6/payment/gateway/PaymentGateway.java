package org.example.templatejava6.payment.gateway;

import java.util.Map;

public interface PaymentGateway {

    String getProviderCode();

    PaymentCreateResult createPayment(PaymentCreateCommand command);

    PaymentCallbackResult verifyCallback(Map<String, String> params);
}
