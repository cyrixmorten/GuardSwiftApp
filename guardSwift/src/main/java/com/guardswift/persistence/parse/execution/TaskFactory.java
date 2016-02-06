package com.guardswift.persistence.parse.execution;

import com.google.common.collect.Lists;
//import com.guardswift.persistence.parse.execution.task.alarm.Alarm;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;

import java.util.List;

/**
 * Created by cyrix on 7/20/15.
 */
public class TaskFactory {

    List<BaseTask> tasks;

    public TaskFactory() {

        tasks = Lists.newArrayList();

//        tasks.add(new Alarm());
        tasks.add(new StaticTask());
        tasks.add(new CircuitUnit());
        tasks.add(new DistrictWatchClient());
    }

    public List<BaseTask> getTasks() {
        return tasks;
    }
}
