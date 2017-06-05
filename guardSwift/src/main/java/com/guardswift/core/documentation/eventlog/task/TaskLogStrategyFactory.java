package com.guardswift.core.documentation.eventlog.task;

import com.google.common.collect.Lists;

import java.util.List;


public class TaskLogStrategyFactory implements LogTaskFactory {

    private List<LogTaskStrategy> logStrategies = Lists.newArrayList();

    public TaskLogStrategyFactory() {
        logStrategies.add(new TaskReportIdLogStrategy());
        logStrategies.add(new TaskLogStrategy());
        logStrategies.add(new TaskStaticLogStrategy());
        logStrategies.add(new TaskRegularLogStrategy());
//        logStrategies.add(new TaskDistrictWatchLogStrategy());
        logStrategies.add(new TaskTypeLogStrategy());
        logStrategies.add(new TaskTypeDescLogStrategy());
        logStrategies.add(new TaskClientLogStrategy());
        logStrategies.add(new TaskIdLogStrategy());
        logStrategies.add(new TaskEventCodeLogStrategy());
        logStrategies.add(new TaskWithinScheduledTimeLogStrategy());

    }

    public List<LogTaskStrategy> getStrategies() {
        return logStrategies;
    }
}
