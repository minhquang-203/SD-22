package org.example.templatejava6.shipping.model.response;

import lombok.Getter;
import lombok.Setter;

/** Mot ca lay hang GHN (API /v2/shift/date), dung khi khach chon thoi diem GHN den lay hang tra. */
@Getter
@Setter
public class GhnPickShiftResponse {

    private Integer id;
    private String title;
    private Long fromTime;
    private Long toTime;

    public GhnPickShiftResponse(Integer id, String title, Long fromTime, Long toTime) {
        this.id = id;
        this.title = title;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }
}
