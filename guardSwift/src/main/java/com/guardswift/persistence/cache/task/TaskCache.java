package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.persistence.parse.execution.ParseTask;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;


@Singleton
public class TaskCache extends BaseTaskCache<ParseTask> {

    @Inject
    TaskCache(@ForApplication Context context) {
        super(ParseTask.class, context);
    }

    @Override
    public ParseTask getConcreteTask() {
        return new ParseTask();
    }

}
