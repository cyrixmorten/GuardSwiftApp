package com.guardswift.persistence.cache.documentaion;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class EventLogCache extends ParseCache<EventLog> {

    private static final String SELECTED = "selected";

    @Inject
    EventLogCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(EventLog.class, context);
    }

    public void setSelected(EventLog eventlog) {
        put(SELECTED, eventlog);
    }

    public EventLog getSelected() {
        return get(SELECTED);
    }

}
