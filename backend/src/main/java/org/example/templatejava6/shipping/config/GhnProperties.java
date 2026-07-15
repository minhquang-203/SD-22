package org.example.templatejava6.shipping.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cau hinh tich hop Giao Hang Nhanh (GHN). Cac gia tri duoc nap tu application.properties
 * voi tien to "ghn.".
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {

    /** Token API cua GHN. */
    private String token;

    /** Base URL cua GHN public API. */
    private String baseUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api";

    /** ShopId cua cua hang tren he thong GHN. */
    private String shopId;

    /** Loai dich vu: 2 = hang nhe (Standard), 5 = hang nang. */
    private Integer serviceTypeId = 2;

    /** Quan/huyen cua kho gui hang. */
    private Integer fromDistrictId;

    /** Phuong/xa cua kho gui hang. */
    private String fromWardCode;

    /** Ten nguoi nhan tai shop (dung cho van don hoan tra hang ve shop). */
    private String shopName = "SUNOVA Shop";

    /** So dien thoai shop (van don hoan tra). */
    private String shopPhone;

    /** Dia chi shop (van don hoan tra). */
    private String shopAddress;

    /** Khoi luong mac dinh moi don (gram). */
    private Integer defaultWeight = 500;

    /** Chieu dai mac dinh kien hang (cm). */
    private Integer defaultLength = 20;

    /** Chieu rong mac dinh kien hang (cm). */
    private Integer defaultWidth = 15;

    /** Chieu cao mac dinh kien hang (cm). */
    private Integer defaultHeight = 10;

    /** Phi van chuyen mac dinh khi khong goi duoc GHN. */
    private Long fallbackFee = 30000L;

    public boolean isConfigured() {
        return token != null && !token.isBlank();
    }

    public boolean isFeeConfigured() {
        return isConfigured()
                && shopId != null && !shopId.isBlank()
                && fromDistrictId != null;
    }
}
