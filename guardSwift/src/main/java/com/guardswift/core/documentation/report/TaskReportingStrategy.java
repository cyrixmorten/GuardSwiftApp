package com.guardswift.core.documentation.report;

import android.content.Context;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.parse.SaveCallback;

import bolts.Task;

/**
 * Created by cyrix on 6/8/15.
 */
public interface TaskReportingStrategy<T extends BaseTask> {

    Task<Report> getReport();
    void addUnique(Context context, EventLog eventLog, SaveCallback savedOnlineCallback);
    void remove(Context context, EventLog eventLog, SaveCallback savedOnlineCallback);

}
