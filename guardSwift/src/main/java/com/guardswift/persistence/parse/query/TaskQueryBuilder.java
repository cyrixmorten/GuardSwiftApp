package com.guardswift.persistence.parse.query;


import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class TaskQueryBuilder extends
        ParseQueryBuilder<ParseTask> {

    public TaskQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));

    }

    @Override
    public ParseQuery<ParseTask> build() {
        query.include(ParseTask.guard);
        query.include(ParseTask.client);
        return super.build();
    }


}
