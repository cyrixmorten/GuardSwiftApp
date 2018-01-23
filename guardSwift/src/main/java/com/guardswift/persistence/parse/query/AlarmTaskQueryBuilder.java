package com.guardswift.persistence.parse.query;


import android.location.Location;

import com.google.common.collect.Sets;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class AlarmTaskQueryBuilder extends
        ParseQueryBuilder<ParseTask> {

    public AlarmTaskQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));

        query.whereEqualTo(ParseTask.taskType, ParseTask.TASK_TYPE_STRING.ALARM);
    }

    @Override
    public ParseQuery<ParseTask> build() {
        query.include(ParseTask.guard);
        query.include(ParseTask.client);
        query.include(ParseTask.client + "." + Client.contacts);
        query.include(ParseTask.client + "." + Client.roomLocations);
        query.include(ParseTask.client + "." + Client.people);
        query.whereExists(ParseTask.client);
        return super.build();
    }


    public ParseQuery<ParseTask> buildNoIncludes() {
        return super.build();
    }


    public AlarmTaskQueryBuilder within(int kilometers, Location fromLocation) {
        ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
        if (parseGeoPoint != null)
            query.whereWithinKilometers(ParseTask.position, parseGeoPoint, kilometers);
        return this;
    }

    public AlarmTaskQueryBuilder matching(Guard guard) {
        query.whereEqualTo(ParseTask.guard, guard);
        return this;
    }

    public AlarmTaskQueryBuilder whereStatus(String... status) {
        query.whereContainedIn(ParseTask.status, Sets.newHashSet(status));
        return this;
    }


    public AlarmTaskQueryBuilder sortNearest() {
        ParseModule.sortNearest(query, ParseTask.position);
        return this;
    }

    public AlarmTaskQueryBuilder sortByCreatedAtDescending() {

        query.addDescendingOrder(ParseTask.createdAt);

        return this;
    }

}
