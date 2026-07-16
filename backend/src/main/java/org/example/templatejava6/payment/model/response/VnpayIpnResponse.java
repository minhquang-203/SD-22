package org.example.templatejava6.payment.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Phản hồi IPN theo đặc tả VNPay (HTTP 200 + JSON).
 */
@Getter
@AllArgsConstructor
public class VnpayIpnResponse {

    @JsonProperty("RspCode")
    private final String rspCode;

    @JsonProperty("Message")
    private final String message;

    public static VnpayIpnResponse of(String rspCode, String message) {
        return new VnpayIpnResponse(rspCode, message);
    }
}
