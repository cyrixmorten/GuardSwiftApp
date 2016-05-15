package com.guardswift.core.documentation.eventlog.task;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by cyrix on 6/7/15.
 */
public class TaskLogStrategyFactory implements LogTaskFactory {

    List<LogTaskStrategy> logStrategies = Lists.newArrayList();

    public TaskLogStrategyFactory() {
        logStrategies.add(new TaskReportIdLogStrategy());
        logStrategies.add(new TaskStaticLogStrategy());
        logStrategies.add(new TaskRegularLogStrategy());
        logStrategies.add(new TaskDistrictWatchLogStrategy());
        logStrategies.add(new TaskTypeLogStrategy());
        logStrategies.add(new TaskTypeDescLogStrategy());
        logStrategies.add(new TaskClientLogStrategy());
        logStrategies.add(new TaskIdLogStrategy());

    }

    public List<LogTaskStrategy> getStrategies() {
        return logStrategies;
    }
}
