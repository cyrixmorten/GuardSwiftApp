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
import com.parse.ParseSession;

import org.json.JSONObject;

import java.util.Date;

@ParseClassName("Guard")
public class Guard extends ExtendedParseObject implements Positioned {

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


    private static final String guardId = "guardId";
    private static final String name = "name";
    private static final String session = "session";
    private static final String mobileNumber = "mobileNumber";

    private static final String alarmNotify = "alarmNotify";
    private static final String alarmSound = "alarmSound";
    private static final String alarmSMS = "alarmSMS";

    private static final String lastLogin = "lastLogin";
    private static final String lastLogout = "lastLogout";
    private static final String lastEvent = "lastEvent";
    private static final String lastLocationUpdate = "lastLocationUpdate";
    private static final String lastGeocodedAddress = "lastGeocodedAddress";
    private static final String position = "position";
    private static final String isOnline = "isOnline";

    // access rights
    private static final String accessRegular = "accessRegular";
    private static final String accessDistrict = "accessDistrict";
    private static final String accessStatic = "accessStatic";

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

        public QueryBuilder hasSession() {
            query.whereExists(Guard.session);
            return this;
        }

    }

    public int getGuardId() {
        return getInt(Guard.guardId);
    }

    public String getName() {
        return getString(Guard.name);
    }

    public void setName(String name) {
        put(Guard.name, name);
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

    public boolean canAccessRegularTasks() {
        return getBoolean(accessRegular);
    }

    public boolean canAccessDistrictTasks() {
        return getBoolean(accessDistrict);
    }

    public boolean canAccessStaticTasks() {
        return getBoolean(accessStatic);
    }

    public boolean canAccessAlarms() {
        return getBoolean(accessRegular); // todo temporary
    }

    public void setSession(ParseSession session) {
        put(Guard.session, session);
    }

    public void setMobile(String mobile) {
        put(Guard.mobileNumber, mobile);
    }

    public String getMobile() {
        return getStringSafe(Guard.mobileNumber);
    }


    public boolean isAlarmNotificationsEnabled() {
        return getBoolean(Guard.alarmNotify);
    }

    public void enableAlarmNotification(boolean enable) {
        put(Guard.alarmNotify, enable);
    }

    public boolean isAlarmSoundEnabled() {
        return isAlarmNotificationsEnabled() && getBoolean(Guard.alarmSound);
    }

    public void enableAlarmSound(boolean enable) {
        if (enable) {
            put(Guard.alarmNotify, true);
        }
        put(Guard.alarmSound, enable);
    }

    public boolean isAlarmSMSEnabled() {
        return isAlarmNotificationsEnabled() && getBoolean(Guard.alarmSMS);
    }

    public void enableAlarmSMS(boolean enable) {
        put(Guard.alarmSMS, enable);
    }

}
