package com.guardswift.core.documentation.eventlog.context;

import android.content.Context;

import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogCurrentGuardStrategy implements LogContextStrategy {

    public static final String guard = "guard";
    public static final String guardId = "guardId";
    public static final String guardName = "guardName";

    private final GuardCache guardCache;

    public LogCurrentGuardStrategy(Context context) {
        this.guardCache = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();
    }

    @Override
    public void log(ParseObject toParseObject) {

        Guard guard = guardCache.getLoggedIn();

        toParseObject.put(LogCurrentGuardStrategy.guard, guard);
        toParseObject.put(LogCurrentGuardStrategy.guardId, guard.getGuardId());
        toParseObject.put(LogCurrentGuardStrategy.guardName, guard.getName());
    }
}
