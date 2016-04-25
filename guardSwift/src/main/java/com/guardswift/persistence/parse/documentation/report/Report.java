package com.guardswift.persistence.parse.documentation.report;

import android.content.Context;

import com.google.common.collect.Lists;
import com.guardswift.core.documentation.eventlog.context.LogContextFactory;
import com.guardswift.core.documentation.eventlog.context.LogContextStrategy;
import com.guardswift.core.documentation.eventlog.task.LogTaskFactory;
import com.guardswift.core.documentation.eventlog.task.LogTaskStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskTypeLogStrategy;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.List;

@ParseClassName("Report")
public class Report extends ExtendedParseObject implements GSReport {


    public static final String reportId = "reportId";
    public static final String reportEntries = "reportEntries";
    public static final String eventLogs = "eventLogs";

    public static final String extraTimeSpent = "extraTimeSpent";
    public static final String eventCount = "eventCount";


    public static Report create(LogContextFactory logFactory, LogTaskFactory taskLogFactory, GSTask task) {

        Report report = new Report();

        report.put(reportId, task.getReportId());
        report.put(owner, ParseUser.getCurrentUser());

        for (LogContextStrategy logStrategy : logFactory.getStrategies()) {
            logStrategy.log(report);
        }

        for (LogTaskStrategy logStrategy : taskLogFactory.getStrategies()) {
            logStrategy.log(task, report);
        }

        return report;
    }

    public List<EventLog> getEventLogs() {
        List<EventLog> logs = getList(eventLogs);
        return (logs != null) ? logs : Lists.<EventLog>newArrayList();
    }


    public synchronized void add(EventLog eventLog) {
        increment(eventCount);
        addUnique(eventLogs, eventLog);
    }

    public synchronized void remove(EventLog eventLog) {
        increment(eventCount, -1);
        addUnique(eventLogs, eventLog);
    }

    public synchronized void addEntry(JSONObject jsonObject) {
        increment(eventCount);
        add(reportEntries, jsonObject);
    }

    @Override
    public void extraTimeSpent(int minutes) {
        put(extraTimeSpent, minutes);
    }

    @Override
    public String getParseClassName() {
        return Report.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Report> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public static class QueryBuilder extends ParseQueryBuilder<Report> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Report.class));
        }

        public QueryBuilder include(String... includes) {
            for (String include: includes) {
                query.include(include);
            }
            return this;
        }

        public QueryBuilder matching(GSTask task) {
            query.whereEqualTo(reportId, task.getReportId());
            return this;
        }

        public QueryBuilder matching(Client client) {
            query.whereEqualTo(EventLog.client, client);
            return this;
        }

        public QueryBuilder matching(GSTask.TASK_TYPE task_type) {
            if (task_type == null) {
                return this;
            }
            query.whereEqualTo(TaskTypeLogStrategy.taskTypeName, task_type.toString());
            return this;
        }

    }

    public String getGuardName() {
        return getString(EventLog.guardName);
    }


}
