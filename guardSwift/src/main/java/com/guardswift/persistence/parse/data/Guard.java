package com.guardswift.persistence.parse.data;

import android.content.Context;
import android.util.Log;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.util.GeocodedAddress;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.Date;

@ParseClassName("Guard")
public class Guard extends ExtendedParseObject implements Positioned {



//    public static class Recent {
//
//        private static String TAG = "Guard.Recent";
//
//        private static Guard selected;
//
//        public static Guard getSelected() {
//            return selected;
//        }
//
//        public static void setSelected(Guard selected) {
//            Recent.selected = selected;
//        }
//
//        public static Guard getSelected(ParseModulePreferences preferences) {
//
//            if (selected != null)
//                return selected;
//
//
//            if (preferences.isGuardLoggedIn()) {
//
//                int guardId = preferences.getGuardid();
//                try {
//                    selected = Query.get(guardId);
//                } catch (ParseException e) {
//                    Log.e(TAG, "getCurrentGuard not found");
//                }
//
//            }
//
//            return selected;
//        }
//    }



    public static class Query {

        private static String TAG = "Guard.Query";



        public static Guard get(String objectId)
                throws ParseException {
            ParseQuery<Guard> query = new Guard.QueryBuilder(true)
                    .matchingObjectId(objectId).build();
            return query.getFirst();
        }

        public static Guard get(int guardId) throws ParseException {
            Log.e(TAG, "getGuard: " + guardId);
            ParseQuery<Guard> query = new Guard.QueryBuilder(true)
                    .build();
            query.whereEqualTo(Guard.guardId, guardId);
            return query.getFirst();
        }

    }


    public static final String guardId = "guardId";
    public static final String name = "name";


    private static String lastLogin = "lastLogin";
    private static String lastLogout = "lastLogout";
    private static String lastEvent = "lastEvent";
    private static String lastLocationUpdate = "lastLocationUpdate";
    private static String lastGeocodedAddress = "lastGeocodedAddress";
    private static String position = "position";
    private static String isOnline = "isOnline";

    @Override
    public String getParseClassName() {
        return Guard.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Guard> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public static class QueryBuilder extends ParseQueryBuilder<Guard> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Guard.class));
        }

        @Override
        public ParseQuery<Guard> build() {
//            query.include(messages);
            query.include(lastEvent);
            query.setLimit(1000);
            return super.build();
        }

    }

    public int getGuardId() {
        return getInt(guardId);
    }

    public String getName() {
        return getString(name);
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    private void setLastLogin(Date lastLogin) {
        put(Guard.lastLogin, lastLogin);
    }

    public Date getLastLogin() {
        return getDate(Guard.lastLogin);
    }

    private void setLastLogout(Date lastLogout) {
        put(Guard.lastLogout, lastLogout);
    }

    public Date getLastLogout() {
        return getDate(Guard.lastLogout);
    }

    public void setLastGeocodedAddress(GeocodedAddress reverseGeocodedAddress) {
        if (reverseGeocodedAddress == null)
            return;
        put(Guard.lastLocationUpdate, new Date());
        put(Guard.lastGeocodedAddress, reverseGeocodedAddress.toJSON());
    }

    public Date getLastLocationUpdate() {
        return getDate(lastLocationUpdate);
    }

    public GeocodedAddress getLastGeocodedAddress() {
        return new GeocodedAddress(getJSONObject(Guard.lastGeocodedAddress));
    }

    public void setLastEvent(EventLog event) {
        put(lastEvent, event);
    }

    public EventLog getLastEvent() {
        return (EventLog) getParseObject(lastEvent);
    }

    public void setOnline(boolean online) {
        if (online) {
            setLastLogin(new Date());
        } else {
            setLastLogout(new Date());
        }
        put(isOnline, online);
    }

    public boolean isOnline() {
        return has(isOnline) && getBoolean(isOnline);
    }

    public void setPosition(ParseGeoPoint location) {
        if (location == null)
            return;

        put(Guard.position, location);
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(position);
    }

}
