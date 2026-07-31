package org.example.templatejava6.realtime.event;

import lombok.Getter;
import org.example.templatejava6.realtime.model.HoTroRealtimeEvent;
import org.springframework.context.ApplicationEvent;

@Getter
public class HoTroRealtimeAppEvent extends ApplicationEvent {

    private final HoTroRealtimeEvent payload;

    public HoTroRealtimeAppEvent(Object source, HoTroRealtimeEvent payload) {
        super(source);
        this.payload = payload;
    }
}
