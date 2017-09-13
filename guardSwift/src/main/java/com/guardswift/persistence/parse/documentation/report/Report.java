package com.guardswift.persistence.parse.documentation.report;

import com.google.common.collect.Lists;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.query.ReportQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

@ParseClassName("Report")
public class Report extends ExtendedParseObject {


    public static final String reportId = "reportId";
    public static final String reportEntries = "reportEntries";
    public static final String eventLogs = "eventLogs";

    public static final String extraTimeSpent = "extraTimeSpent";
    public static final String eventCount = "eventCount";


//    public static Report create(LogContextFactory logFactory, LogTaskFactory taskLogFactory, ParseTask task) {
//
//        Report report = new Report();
//
//        report.put(reportId, task.getReportId());
//
//        for (LogContextStrategy logStrategy : logFactory.getStrategies()) {
//            logStrategy.log(report);
//        }
//
//        for (LogTaskStrategy logStrategy : taskLogFactory.getStrategies()) {
//            logStrategy.log(task, report);
//        }
//
//        report.setDefaultOwner();
//
//        return report;
//    }

    public List<EventLog> getEventLogs() {
        List<EventLog> logs = getList(eventLogs);
        return (logs != null) ? logs : Lists.<EventLog>newArrayList();
    }


    @Override
    public String getParseClassName() {
        return Report.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Report> getAllNetworkQuery() {
        return new ReportQueryBuilder(false).build();
    }


    public static ReportQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new ReportQueryBuilder(fromLocalDatastore);
    }

    public Date getDeviceTimestamp() {
        return getDate(EventLog.deviceTimestamp);
    }


    public String getGuardName() {
        return getString(EventLog.guardName);
    }


}
