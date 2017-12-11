package com.guardswift.persistence.parse.query;


import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class ReportQueryBuilder extends
        ParseQueryBuilder<Report> {

    public ReportQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(Report.class));
    }



    public ReportQueryBuilder include(String... includes) {
        for (String include: includes) {
            query.include(include);
        }
        return this;
    }


    public ReportQueryBuilder matching(ParseTask task) {

        query.whereEqualTo(Report.tasks, task);

        return this;
    }


    public ReportQueryBuilder matching(Client client) {
        query.whereEqualTo(EventLog.client, client);
        return this;
    }

    public ReportQueryBuilder matching(ParseTask.TASK_TYPE task_type) {
        if (task_type == null) {
            return this;
        }
        query.whereEqualTo(EventLog.taskTypeName, task_type.toString());
        return this;
    }

}
