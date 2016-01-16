package com.guardswift.persistence.parse.execution;

import com.google.common.collect.Lists;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.regular.CircuitUnit;

import java.util.List;

/**
 * Created by cyrix on 7/20/15.
 */
public class TaskFactory {

    List<BaseTask> tasks;

    public TaskFactory() {

        tasks = Lists.newArrayList();

        tasks.add(new Alarm());
        tasks.add(new CircuitUnit());
        tasks.add(new DistrictWatchClient());
    }

    public List<BaseTask> getTasks() {
        return tasks;
    }
}
