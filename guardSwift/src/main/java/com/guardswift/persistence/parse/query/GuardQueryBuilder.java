package com.guardswift.persistence.parse.query;


import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class GuardQueryBuilder extends
        ParseQueryBuilder<Guard> {

    public GuardQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(Guard.class));
    }

    @Override
    public ParseQuery<Guard> build() {
//            query.include(messages);
        query.include(Guard.installation);
        query.setLimit(1000);
        return super.build();
    }

    public GuardQueryBuilder hasSession() {
        query.whereExists(Guard.session);
        return this;
    }

    public GuardQueryBuilder sortByName(boolean ascending) {
        if (ascending) {
            query.addAscendingOrder(Guard.name);
        } else {
            query.addDescendingOrder(Guard.name);
        }
        return this;
    }

}
