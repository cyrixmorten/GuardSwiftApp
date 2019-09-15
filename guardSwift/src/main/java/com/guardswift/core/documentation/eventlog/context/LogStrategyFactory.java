package com.guardswift.core.documentation.eventlog.context;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by cyrix on 6/7/15.
 */
public class LogStrategyFactory implements LogContextFactory {

    private List<LogContextStrategy> logStrategies = Lists.newArrayList();

    public LogStrategyFactory() {

        logStrategies.add(new LogCurrentGuardStrategy());
        logStrategies.add(new LogCurrentActivityStrategy());
        logStrategies.add(new LogCurrentLocationStrategy());
        logStrategies.add(new LogDeviceInfoStrategy());
    }

    public List<LogContextStrategy> getStrategies() {
        return logStrategies;
    }
}
