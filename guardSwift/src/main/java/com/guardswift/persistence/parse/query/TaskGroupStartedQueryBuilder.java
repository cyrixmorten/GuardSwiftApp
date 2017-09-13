package com.guardswift.persistence.parse.query;

import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class TaskGroupStartedQueryBuilder extends ParseQueryBuilder<TaskGroupStarted> {

    private static final String TAG = TaskGroupStartedQueryBuilder.class.getSimpleName();


    public TaskGroupStartedQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(TaskGroupStarted.class));
    }



    @Override
    public ParseQuery<TaskGroupStarted> build() {
        query.include(TaskGroupStarted.taskGroup);
        whereActive();
        return super.build();
    }

    public TaskGroupStartedQueryBuilder sortByName() {
        query.addAscendingOrder(TaskGroupStarted.name);
        return this;
    }

    public TaskGroupStartedQueryBuilder withObjectId(String objectId) {
        query.whereEqualTo(TaskGroupStarted.objectId, objectId);
        return this;
    }

    public TaskGroupStartedQueryBuilder whereActive() {
        query.whereDoesNotExist(TaskGroupStarted.timeEnded);
        return this;
    }

    public TaskGroupStartedQueryBuilder matching(TaskGroup taskGroup) {
        query.whereEqualTo(TaskGroupStarted.taskGroup, taskGroup);
        return this;
    }
}
