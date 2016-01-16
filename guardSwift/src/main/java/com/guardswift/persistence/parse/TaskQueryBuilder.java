package com.guardswift.persistence.parse;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.parse.ParseQuery;

/**
 * Created by cyrix on 11/14/15.
 */
public abstract class TaskQueryBuilder<T extends BaseTask> extends ParseQueryBuilder<T> {

    public TaskQueryBuilder(String pin, boolean fromLocalDatastore, ParseQuery<T> query) {
        super(pin, fromLocalDatastore, query);
    }

    @Override
    public ParseQuery<T> build() {
        return super.build();
    }

    public abstract ParseQuery<T> buildNoIncludes();



}
