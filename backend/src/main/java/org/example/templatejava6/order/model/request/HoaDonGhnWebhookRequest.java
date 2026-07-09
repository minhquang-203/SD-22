package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HoaDonGhnWebhookRequest {

    /** Ma trang thai van don GHN (vd: delivered, picking, returned). */
    @NotBlank
    private String status;

    private String ghiChu;
}
