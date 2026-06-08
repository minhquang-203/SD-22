package org.example.templatejava6.product.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnhSanPhamRequest {

    /** URL đã lưu (khi cập nhật giữ ảnh cũ). Ảnh mới upload dùng fileIndex. */
    private String url;

    /** Chỉ số file trong multipart `files` tương ứng ảnh mới. */
    private Integer fileIndex;

    private Boolean laAnhChinh;
    private Integer thuTu;
}
