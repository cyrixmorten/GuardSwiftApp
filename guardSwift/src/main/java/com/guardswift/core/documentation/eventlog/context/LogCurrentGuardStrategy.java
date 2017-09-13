package com.guardswift.core.documentation.eventlog.context;

import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseObject;

public class LogCurrentGuardStrategy implements LogContextStrategy {



    private final GuardCache guardCache;

    public LogCurrentGuardStrategy() {
        this.guardCache = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();
    }

    @Override
    public void log(ParseObject toParseObject) {
        Guard guard = guardCache.getLoggedIn();
        if (guard == null || toParseObject == null) {
            return;
        }

        toParseObject.put(EventLog.guard, guard);
        toParseObject.put(EventLog.guardId, guard.getGuardId());
        toParseObject.put(EventLog.guardName, guard.getName());
    }
}
