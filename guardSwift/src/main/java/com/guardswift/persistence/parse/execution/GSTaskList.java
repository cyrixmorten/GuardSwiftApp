package com.guardswift.persistence.parse.execution;

/**
 * Created by cyrix on 10/27/15.
 */
public interface GSTaskList {
    GSTask.TASK_TYPE getTaskType();
    String getName();
}
