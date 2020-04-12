package com.guardswift.persistence.parse.data.client;

import androidx.annotation.NonNull;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("ClientLocation")
public class ClientLocation extends ExtendedParseObject  {
    

    private static final String location = "location";
    private static final String isCheckpoint = "isCheckpoint";



    public static ClientLocation create(String location) {
        ClientLocation clientLocation = new ClientLocation();
        clientLocation.put(ClientLocation.location, location.trim());
        clientLocation.put(owner, ParseUser.getCurrentUser());
        return clientLocation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<ClientLocation> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }



    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    @Override
    public int compareTo(@NonNull ExtendedParseObject object) {
        if (object instanceof ClientLocation) {
            return getLocation().compareTo(((ClientLocation)object).getLocation());
        }

        return 0;
    }


    public static class QueryBuilder extends ParseQueryBuilder<ClientLocation> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                    .getQuery(ClientLocation.class));
        }

        public QueryBuilder matchingClient(Client client) {
            ParseQuery<ClientLocation> query = build();
            query.orderByDescending("updatedAt");
            return this;
        }
    }

    public void setName(String name) {
        put(ClientLocation.location, name.trim());
    }

    public boolean isCheckpoint() {
        return has(isCheckpoint) && getBoolean(isCheckpoint);
    }

    public String getLocation() {
        if (!has(location)) {
            return "";
        }
        return getString(location);
    }



}
