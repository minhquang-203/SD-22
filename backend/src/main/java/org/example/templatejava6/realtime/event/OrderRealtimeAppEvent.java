package org.example.templatejava6.realtime.event;

import lombok.Getter;
import org.example.templatejava6.realtime.model.OrderRealtimeEvent;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderRealtimeAppEvent extends ApplicationEvent {

    private final OrderRealtimeEvent payload;

    public OrderRealtimeAppEvent(Object source, OrderRealtimeEvent payload) {
        super(source);
        this.payload = payload;
    }
}
