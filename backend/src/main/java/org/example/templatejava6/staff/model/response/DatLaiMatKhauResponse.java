package org.example.templatejava6.staff.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatLaiMatKhauResponse {

    private String message;

    /** Chỉ có khi sinh mật khẩu tạm — hiển thị 1 lần cho quản lý */
    private String matKhauTam;
}
