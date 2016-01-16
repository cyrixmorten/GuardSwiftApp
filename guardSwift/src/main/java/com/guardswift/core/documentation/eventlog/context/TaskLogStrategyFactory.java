package com.guardswift.core.documentation.eventlog.context;

import com.google.common.collect.Lists;
import com.guardswift.core.documentation.eventlog.task.TaskAlarmLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskClientLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskDistrictWatchLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskIdLogStrategy;
import com.guardswift.core.documentation.eventlog.task.LogTaskFactory;
import com.guardswift.core.documentation.eventlog.task.LogTaskStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskRegularLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskTypeDescLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskTypeLogStrategy;

import java.util.List;

/**
 * Created by cyrix on 6/7/15.
 */
public class TaskLogStrategyFactory implements LogTaskFactory {

    List<LogTaskStrategy> logStrategies = Lists.newArrayList();

    public TaskLogStrategyFactory() {
        logStrategies.add(new TaskTypeLogStrategy());
        logStrategies.add(new TaskTypeDescLogStrategy());
        logStrategies.add(new TaskRegularLogStrategy());
        logStrategies.add(new TaskAlarmLogStrategy());
        logStrategies.add(new TaskDistrictWatchLogStrategy());
        logStrategies.add(new TaskClientLogStrategy());
        logStrategies.add(new TaskIdLogStrategy());

    }

    public List<LogTaskStrategy> getStrategies() {
        return logStrategies;
    }
}
