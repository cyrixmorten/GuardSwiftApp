package com.guardswift.core.documentation.eventlog.context;

import android.content.Context;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by cyrix on 6/7/15.
 */
public class LogStrategyFactory implements LogContextFactory {

    List<LogContextStrategy> logStrategies = Lists.newArrayList();

    public LogStrategyFactory(Context context) {
        logStrategies.add(new LogCurrentGuardStrategy(context));
        logStrategies.add(new LogCurrentActivityStrategy());
        logStrategies.add(new LogCurrentLocationStrategy());
        logStrategies.add(new LogDeviceInfoStrategy());
        logStrategies.add(new LogTimestampStrategy());
    }

    public List<LogContextStrategy> getStrategies() {
        return logStrategies;
    }
}
