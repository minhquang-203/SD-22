package org.example.templatejava6.product;

/**
 * Giới hạn ảnh sản phẩm — giữ đồng bộ với frontend/src/constants/productImages.js
 */
public final class ProductImageLimits {

    public static final int MAX_COUNT = 10;
    public static final long MAX_BYTES = 15L * 1024 * 1024;
    public static final String MAX_FILE_SIZE_LABEL = "15MB";

    private ProductImageLimits() {}
}
