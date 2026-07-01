package org.example.templatejava6.shipping.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateShippingOrderRequest {

    @NotBlank(message = "Vui lòng nhập tên người nhận")
    private String toName;

    @NotBlank(message = "Vui lòng nhập số điện thoại người nhận")
    private String toPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ người nhận")
    private String toAddress;

    @NotNull(message = "Vui lòng chọn quận/huyện người nhận")
    private Integer toDistrictId;

    @NotBlank(message = "Vui lòng chọn phường/xã người nhận")
    private String toWardCode;

    /** So tien thu ho (COD). */
    private Long codAmount;

    /** Gia tri khai gia. */
    private Long insuranceValue;

    /** Khoi luong (gram). */
    private Integer weight;

    private String note;

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private String name;
        private Integer quantity;
        private Integer weight;
    }
}
