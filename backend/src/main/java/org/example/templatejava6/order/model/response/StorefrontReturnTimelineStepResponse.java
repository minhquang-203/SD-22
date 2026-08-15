package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StorefrontReturnTimelineStepResponse {

    private String ma;
    private String label;
    private LocalDateTime thoiGian;
    /** done | active | pending */
    private String trangThai;
}
