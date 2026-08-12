package org.example.templatejava6.realtime.event;

import lombok.Getter;
import org.example.templatejava6.realtime.model.CustomerNotificationEvent;
import org.springframework.context.ApplicationEvent;

@Getter
public class CustomerNotificationAppEvent extends ApplicationEvent {

    private final CustomerNotificationEvent payload;

    public CustomerNotificationAppEvent(Object source, CustomerNotificationEvent payload) {
        super(source);
        this.payload = payload;
    }
}
