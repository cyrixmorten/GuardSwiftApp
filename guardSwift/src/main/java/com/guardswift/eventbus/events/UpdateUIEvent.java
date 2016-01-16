package com.guardswift.eventbus.events;

public class UpdateUIEvent {

    private final Object object;

    public UpdateUIEvent(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
