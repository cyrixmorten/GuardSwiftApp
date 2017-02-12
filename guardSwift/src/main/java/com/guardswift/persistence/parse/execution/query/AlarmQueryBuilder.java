package com.guardswift.persistence.parse.execution.query;


import android.location.Location;

import com.google.common.collect.Sets;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.TaskQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class AlarmQueryBuilder extends
        TaskQueryBuilder<ParseTask> {

    public AlarmQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));

        query.whereEqualTo(ParseTask.taskType, "Alarm");
    }

    @Override
    public ParseQuery<ParseTask> build() {
        query.include(ParseTask.guard);
        query.include(ParseTask.client);
        return super.build();
    }


    public ParseQuery<ParseTask> buildNoIncludes() {
        return super.build();
    }


    public AlarmQueryBuilder within(int kilometers, Location fromLocation) {
        ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
        if (parseGeoPoint != null)
            query.whereWithinKilometers(ParseTask.position, parseGeoPoint, kilometers);
        return this;
    }

    public AlarmQueryBuilder matching(Guard guard) {
        query.whereEqualTo(ParseTask.guard, guard);
        return this;
    }

    public AlarmQueryBuilder whereStatus(String... status) {
        query.whereContainedIn(ParseTask.status, Sets.newHashSet(status));
        return this;
    }


    public AlarmQueryBuilder sortNearest() {
        ParseModule.sortNearest(query, ParseTask.position);
        return this;
    }

    public AlarmQueryBuilder sortByTimeStarted() {
        query.addDescendingOrder(ParseTask.timeStarted);
        return this;
    }

    public AlarmQueryBuilder sortByCreatedAtDescending() {

        query.addDescendingOrder(ParseTask.createdAt);

        return this;
    }

}
