package org.example.templatejava6.shipping.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GhnDistrictResponse {
    private Integer districtId;
    private String districtName;
    private Integer provinceId;
}
