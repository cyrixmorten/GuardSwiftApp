package com.guardswift.persistence.parse.query;


import android.location.Location;

import com.google.common.collect.Lists;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;


public class TaskQueryBuilder extends
        ParseQueryBuilder<ParseTask> {

    public TaskQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));
    }


    public TaskQueryBuilder within(int kilometers, Location fromLocation) {
        ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
        query.whereWithinKilometers(ParseTask.position, parseGeoPoint, kilometers);
        return this;
    }

    public TaskQueryBuilder matchingTaskTypes(ArrayList<String> taskTypeStrings) {
        query.whereContainedIn(ParseTask.taskType, taskTypeStrings);
        return this;
    }

    public TaskQueryBuilder notMarkedFinished() {
        query.whereNotEqualTo(ParseTask.status, ParseTask.STATUS.FINISHED);
        return this;
    }

    @Override
    public ParseQuery<ParseTask> build() {
        query.include(ParseTask.guard);
        query.include(ParseTask.client);
        return super.build();
    }


}
