package com.guardswift.persistence.parse.documentation.report;

import com.guardswift.persistence.parse.documentation.event.EventLog;

/**
 * Created by cyrix on 6/8/15.
 */
public interface GSReport {

    /*
     * Add an entry to the report
     */
    void add(EventLog eventLog);
    void extraTimeSpent(int minutes);

//    /*
//     * Locate report for the task
//     */
//    GSReport findReport(GSTask task);
//
//    /*
//     * Locate report for the task
//     */
//    GSReport findReportInBackground(GSTask task, GetCallback<ParseObject> );

}
