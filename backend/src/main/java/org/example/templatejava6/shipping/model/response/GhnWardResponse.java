package org.example.templatejava6.shipping.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GhnWardResponse {
    /** Với địa chỉ mới: String.valueOf(_id) từ GHN v3. */
    private String wardCode;
    private String wardName;
    /** Province _id (GHN v3 parent_id). */
    private Integer provinceId;
}
