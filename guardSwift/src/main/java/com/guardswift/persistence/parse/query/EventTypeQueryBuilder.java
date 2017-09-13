package com.guardswift.persistence.parse.query;


import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class EventTypeQueryBuilder extends
        ParseQueryBuilder<EventType> {

    public EventTypeQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(EventType.class));
    }

    @Override
    public ParseQuery<EventType> build() {
        query.setLimit(1000);
        return super.build();
    }

    @SuppressWarnings("unchecked")
    public EventTypeQueryBuilder matchingIncludes(Client client) {

        ParseQuery<EventType> queryClientEventType = ParseQuery
                .getQuery(EventType.class);
        queryClientEventType.whereEqualTo(EventType.client, client);

        ParseQuery<EventType> queryEventType = ParseQuery
                .getQuery(EventType.class);
        queryEventType.whereDoesNotExist(EventType.client);

        appendQueries(queryClientEventType, queryEventType);

        return this;
    }

    public EventTypeQueryBuilder sortByTimesUsed() {
        query.addDescendingOrder(EventType.timesUsed);
        return this;
    }

}
