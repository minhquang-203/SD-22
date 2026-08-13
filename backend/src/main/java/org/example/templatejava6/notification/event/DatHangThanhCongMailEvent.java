package org.example.templatejava6.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Phát khi đơn online đã được xác nhận thanh toán/đặt thành công.
 * Gửi mail hóa đơn được xử lý AFTER_COMMIT để không giữ transaction/lock trong lúc gọi SMTP.
 */
@Getter
public class DatHangThanhCongMailEvent extends ApplicationEvent {

    private final Integer idHoaDon;

    public DatHangThanhCongMailEvent(Object source, Integer idHoaDon) {
        super(source);
        this.idHoaDon = idHoaDon;
    }
}
