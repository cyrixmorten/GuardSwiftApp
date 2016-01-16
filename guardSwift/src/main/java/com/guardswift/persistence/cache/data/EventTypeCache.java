package com.guardswift.persistence.cache.data;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.data.EventType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class EventTypeCache extends ParseCache<EventType> {

    private static final String SELECTED = "selected";

    @Inject
    EventTypeCache(@ForApplication Context context) {
        super(EventType.class, context);
    }

    public void setSelected(EventType eventType) {
        put(SELECTED, eventType);
    }

    public EventType getSelected() {
        return get(SELECTED);
    }

    public void clearSelected() {
        remove(SELECTED);
    }
}
