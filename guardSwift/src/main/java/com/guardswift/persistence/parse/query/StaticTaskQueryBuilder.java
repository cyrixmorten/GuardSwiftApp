package com.guardswift.persistence.parse.query;

import android.location.Location;

import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;

import java.util.Date;


public class StaticTaskQueryBuilder extends ParseQueryBuilder<ParseTask> {

    public static final int SORTBY_NEAREST = 1;
    public static final int SORTBY_ID = 2;

    private static final String TAG = StaticTaskQueryBuilder.class.getSimpleName();
    

    public StaticTaskQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));

        query.whereEqualTo(ParseTask.taskType, ParseTask.TASK_TYPE_STRING.STATIC);
    }
    


    @Override
    public ParseQuery<ParseTask> build() {
        query.include(ParseTask.client);
        query.include(ParseTask.client + "." + Client.contacts);
        query.include(ParseTask.client + "." + Client.roomLocations);
        query.include(ParseTask.client + "." + Client.people);
        query.include(ParseTask.guard);
        query.whereExists(ParseTask.client);

        query.setLimit(1000);

        return super.build();
    }
    

    public ParseQuery<ParseTask> buildNoIncludes() {
        query.setLimit(1000);
        return super.build();
    }


    public StaticTaskQueryBuilder matching(Guard guard) {

        query.whereEqualTo(ParseTask.guard, guard);

        return this;
    }

    public StaticTaskQueryBuilder notMatching(ParseTask staticTask) {

        if (staticTask != null)
            query.whereNotEqualTo(ParseTask.objectId, staticTask.getObjectId());

        return this;
    }

    public StaticTaskQueryBuilder daysBack(int days) {
        Date oneWeekAgo = new DateTime().minusDays(days).toDate();
        query.whereGreaterThan(ParseTask.createdAt, oneWeekAgo);

        return this;
    }

    public StaticTaskQueryBuilder status(String status) {
        query.whereEqualTo(ParseTask.status, status);
        return this;
    }

    public StaticTaskQueryBuilder sortedByUpdated() {
        query.orderByDescending(ParseTask.updatedAt);
        return this;
    }

    public StaticTaskQueryBuilder sortedByCreated() {
        query.orderByDescending(ParseTask.createdAt);
        return this;
    }

    public StaticTaskQueryBuilder within(int kilometers, Location fromLocation) {
        ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
        query.whereWithinKilometers(ParseTask.position, parseGeoPoint, kilometers);
        return this;
    }

}
