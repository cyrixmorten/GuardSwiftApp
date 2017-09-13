package com.guardswift.persistence.parse.query;


import android.util.Log;

import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import static com.guardswift.persistence.parse.documentation.event.EventRemark.remark;


public class EventRemarkQueryBuilder extends
        ParseQueryBuilder<EventRemark> {

    private static final String TAG = EventRemarkQueryBuilder.class.getSimpleName();

    public EventRemarkQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(EventRemark.class));

    }
    

    @Override
    public ParseQuery<EventRemark> build() {
        return super.build();
    }

    public EventRemarkQueryBuilder matching(EventType eventType) {
        if (eventType == null) {
            Log.e(TAG, "Missing eventType");
            return this;
        }
        query.whereEqualTo(EventRemark.eventType, eventType);
        return this;
    }

    public EventRemarkQueryBuilder matching(Client client) {
        query.whereEqualTo(EventRemark.client, client);
        return this;
    }

    public EventRemarkQueryBuilder matching(String location) {
        query.whereEqualTo(EventRemark.location, location);
        return this;
    }

    public EventRemarkQueryBuilder sortByTimesUsed() {
        // query.addDescendingOrder(timesUsed);
        query.addAscendingOrder(remark);
        return this;
    }

}
