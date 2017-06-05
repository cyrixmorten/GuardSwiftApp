package com.guardswift.core.documentation.report;

import com.guardswift.persistence.parse.documentation.report.Report;

import bolts.Task;

public interface TaskReportingStrategy {

    Task<Report> getReport();
//    void addUnique(Context context, EventLog eventLog, SaveCallback savedOnlineCallback);
//    void remove(Context context, EventLog eventLog, SaveCallback savedOnlineCallback);

}
