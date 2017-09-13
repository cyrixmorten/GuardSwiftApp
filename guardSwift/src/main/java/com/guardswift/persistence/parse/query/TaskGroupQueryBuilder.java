package com.guardswift.persistence.parse.query;

import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class TaskGroupQueryBuilder extends ParseQueryBuilder<TaskGroup> {

    private static final String TAG = TaskGroupQueryBuilder.class.getSimpleName();


    public TaskGroupQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(TaskGroup.class));
    }

    public TaskGroupQueryBuilder sortByName() {
        query.addAscendingOrder(TaskGroup.name);
        return this;
    }


}
