package org.example.templatejava6.shipping.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GhnWardResponse {
    private String wardCode;
    private String wardName;
    private Integer districtId;
}
