package com.guardswift.eventbus.events;



public class UpdateUIEvent {

    public enum ACTION {CREATE, UPDATE, DELETE}

    private final Object object;
    private final ACTION action;

    public UpdateUIEvent(Object object) {
        this.object = object;
        this.action = ACTION.UPDATE;
    }

    public UpdateUIEvent(Object object, ACTION action) {
        this.object = object;
        this.action = action;
    }

    public Object getObject() {
        return object;
    }

    public ACTION getAction() {
        return action;
    }
}
