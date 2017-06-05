package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/7/15.
 */
public class TaskStaticLogStrategy implements LogTaskStrategy {

    public static final String staticTask = "staticTask";


    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        if (task instanceof StaticTask) {

            StaticTask staticTask = (StaticTask)task;

            toParseObject.put(TaskStaticLogStrategy.staticTask, ParseObject.createWithoutData(StaticTask.class, staticTask.getObjectId()));

        }

    }
}
