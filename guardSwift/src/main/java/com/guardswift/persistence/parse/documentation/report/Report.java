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


    public static final String tasks = "tasks";
    public static final String eventLogs = "eventLogs";


    public List<EventLog> getEventLogs() {
        List<EventLog> logs = getList(eventLogs);
        return (logs != null) ? logs : Lists.newArrayList();
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
