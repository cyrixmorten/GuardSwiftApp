package com.guardswift.core.documentation.report;

import android.content.Context;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.parse.SaveCallback;

import bolts.Task;

public interface TaskReportingStrategy {

    Task<Report> getReport();
    void addUnique(Context context, EventLog eventLog, SaveCallback savedOnlineCallback);
    void remove(Context context, EventLog eventLog, SaveCallback savedOnlineCallback);

}
