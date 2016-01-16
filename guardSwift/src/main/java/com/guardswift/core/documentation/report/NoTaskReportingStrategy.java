package com.guardswift.core.documentation.report;

import android.content.Context;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.parse.SaveCallback;

/**
 * Created by cyrix on 6/8/15.
 */
public class NoTaskReportingStrategy<T extends BaseTask> implements TaskReportingStrategy<T> {

    private static final String TAG = NoTaskReportingStrategy.class.getSimpleName();

    @Override
    public void addUnique(Context context, final EventLog eventLog, SaveCallback saveCallback) {
        // do nothing        ;
    }

    @Override
    public void remove(Context context, EventLog eventLog, SaveCallback saveCallback) {

    }
}
