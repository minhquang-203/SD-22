package org.example.templatejava6.payment.gateway;

/**
 * Cong hoan tien nha cung cap. Them vi moi = implement interface nay + dang ky Spring bean.
 */
public interface RefundGateway {

    String getProviderCode();

    RefundResult refund(RefundCommand command);
}
