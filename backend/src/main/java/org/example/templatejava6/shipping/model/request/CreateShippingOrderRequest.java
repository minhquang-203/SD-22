package org.example.templatejava6.shipping.model.request;

import jakarta.validation.constraints.NotBlank;
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

    /** true = địa chỉ nhận theo đơn vị hành chính mới 2 cấp (GHN v3). */
    private Boolean isNewToAddress;

    /** Tên tỉnh/thành (bắt buộc khi isNewToAddress). */
    private String toProvinceName;

    /** Tên phường/xã (dùng khi isNewToAddress). */
    private String toWardName;

    /** Legacy: mã phường/xã GHN cũ. Địa chỉ 2 cấp không dùng field này. */
    private String toWardCode;

    /** Legacy: mã quận/huyện GHN cũ. */
    private Integer toDistrictId;

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
