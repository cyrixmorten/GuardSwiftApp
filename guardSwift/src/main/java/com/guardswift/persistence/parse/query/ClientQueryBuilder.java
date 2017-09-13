package com.guardswift.persistence.parse.query;


import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.client.Client;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class ClientQueryBuilder extends
        ParseQueryBuilder<Client> {

    public enum SORT_BY {NAME, DISTANCE}
    
    public ClientQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(Client.class));
    }

    @Override
    public ParseQuery<Client> build() {
        query.setLimit(1000);
        query.include(Client.roomLocations);
        query.include(Client.people);
        return super.build();
    }

    public ClientQueryBuilder matching(Client client) {
        query.whereEqualTo(Client.objectId, client.getObjectId());
        return this;
    }

    public ClientQueryBuilder sort(SORT_BY sortBy) {
        if (sortBy == SORT_BY.NAME) {
            return sortByName();
        }
        if (sortBy == SORT_BY.DISTANCE) {
            return sortByDistance();
        }

        return this;
    }

    public ClientQueryBuilder notAutomatic() {
        query.whereNotEqualTo(Client.automatic, true);
        return this;
    }

    ClientQueryBuilder sortByName() {
        query.orderByAscending(Client.name);
        return this;
    }

    ClientQueryBuilder sortByDistance() {
        ParseModule.sortNearest(query,
                Client.position);
        return this;
    }
}
