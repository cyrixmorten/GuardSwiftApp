package com.guardswift.core.tasks.automation;

import com.guardswift.persistence.parse.execution.BaseTask;

/**
 * Created by cyrix on 6/7/15.
 */
public interface TaskAutomationStrategy<T extends BaseTask> {

    void automaticArrival();
    void automaticDeparture();
}
