package org.example.templatejava6.realtime.event;

import lombok.Getter;
import org.example.templatejava6.realtime.model.AdminNotificationEvent;
import org.springframework.context.ApplicationEvent;

@Getter
public class AdminNotificationAppEvent extends ApplicationEvent {

    private final AdminNotificationEvent payload;

    public AdminNotificationAppEvent(Object source, AdminNotificationEvent payload) {
        super(source);
        this.payload = payload;
    }
}
