package com.guardswift.core.tasks.automation;

import com.guardswift.persistence.parse.execution.BaseTask;

/**
 * Created by cyrix on 6/7/15.
 */
public class NoAutomationStrategy<T extends BaseTask> implements TaskAutomationStrategy<T> {

    private static final String TAG = NoAutomationStrategy.class.getSimpleName();


    public NoAutomationStrategy() {
    }


    @Override
    public void automaticArrival() {
    }

    @Override
    public void automaticDeparture() {
    }


}
