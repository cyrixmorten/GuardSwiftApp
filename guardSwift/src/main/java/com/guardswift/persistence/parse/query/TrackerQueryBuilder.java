package com.guardswift.persistence.parse.query;


import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;


public class TrackerQueryBuilder extends
        ParseQueryBuilder<Tracker> {

    public TrackerQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(Tracker.class));
    }

    @Override
    public ParseQuery<Tracker> build() {
        query.include(Tracker.guard);
        return super.build();
    }

    public TrackerQueryBuilder matching(Guard guard) {
        query.whereEqualTo(Tracker.guard, guard);
        return this;
    }

    public TrackerQueryBuilder inProgress(boolean inProgress) {
        query.whereEqualTo(Tracker.inProgress, inProgress);
        return this;
    }

    public TrackerQueryBuilder matching(Date date) {
        query.whereEqualTo(Tracker.sampleStart, date);
        return this;
    }

}
