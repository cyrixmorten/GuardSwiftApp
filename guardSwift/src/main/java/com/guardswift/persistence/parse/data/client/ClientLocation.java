package com.guardswift.persistence.parse.data.client;

import android.content.Context;
import android.util.Log;

import com.guardswift.core.ca.FingerprintingModule;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import dk.alexandra.positioning.wifi.AccessPoint;
import dk.alexandra.positioning.wifi.Fingerprint;
import dk.alexandra.positioning.wifi.io.WiFiIO;

@ParseClassName("ClientLocation")
public class ClientLocation extends ExtendedParseObject implements Comparable<ClientLocation> {


    // TODO
    public void adjustFingerprint(Set<AccessPoint> sample) {
//        JSONObject fingerprint = getFingerprint();
    }



//    public static class Recent {
//
//        private static String TAG = "ClientLocation.Recent";
//
//        private static ClientLocation blacklistedCheckpoint;
//        private static ClientLocation nearCheckpoint;
//        private static int hits;
//
//        public static ClientLocation getNearCheckpoint() {
//            return nearCheckpoint;
//        }
//
//        public static void setNearCheckpoint(ClientLocation checkpoint) {
//            if (nearCheckpoint != null && nearCheckpoint.equals(checkpoint)) {
//                hits++;
//                Log.d(TAG, "increase counter: " + hits);
//            } else {
//                Recent.nearCheckpoint = checkpoint;
//                hits = 0;
//                Log.d(TAG, "reset counter: " + hits);
//            }
//        }
//
//        public static int getHits() {
//            return hits;
//        }
//
//        public static void resetHits() {
//            hits = 0;
//        }
//
//        public static void setBlacklisted(ClientLocation checkpoint) {
//            blacklistedCheckpoint = checkpoint;
//        }
//
//        public static boolean isBlacklisted(ClientLocation checkpoint) {
//            return (blacklistedCheckpoint == null) ? false : blacklistedCheckpoint.equals(checkpoint);
//        }
//    }
    

    private static final String location = "location";
    private static final String isCheckpoint = "isCheckpoint";
    private static final String fingerprint = "fingerprint";
    private static final String wifisamples = "wifisamples";
    // local
    private static final String last_trigger_distance = "last_trigger_distance";
    private static final String last_trigger_probability = "last_trigger_probability";
    private static final String isChecked = "isChecked";

    @Override
    public String getParseClassName() {
        return ClientLocation.class.getSimpleName();
    }

    public static ClientLocation create(String location, boolean isCheckpoint) {
        ClientLocation clientLocation = new ClientLocation();
        clientLocation.put(ClientLocation.location, location.trim());
        clientLocation.put(ClientLocation.isCheckpoint, isCheckpoint);
        clientLocation.put(owner, ParseUser.getCurrentUser());
        return clientLocation;
    }

    ;

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<ClientLocation> getAllNetworkQuery() {
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

    @Override
    public int compareTo(ClientLocation another) {
        return getLocation().compareTo(another.getLocation());
    }

    public String getFingerprintString() {
        if (getFingerprint() != null) {
            Log.d(TAG, "getFingerprintString() " + has(fingerprint));
            Log.d(TAG, "getFingerprintString() " + getFingerprint().toString());
            return getFingerprint().toString();
        }
        Log.e(TAG, "Checkpoint has no fingerprint: " + getLocation());
        return null;
    }

    ;

    public JSONObject getFingerprint() {
        return getJSONObject(fingerprint);
    }

    public void storeSamples(Set<AccessPoint> samples) {
        put(ClientLocation.wifisamples, FingerprintingModule.convertToJsonArray(samples));
    }

    public JSONArray getWifiSamples() {
        return getJSONArray(wifisamples);
    }

    public void storeFingerprint(Fingerprint fingerprint) {
        String jsonString = WiFiIO.convertToJSON(fingerprint);
        try {
            put(ClientLocation.fingerprint, new JSONObject(jsonString));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Does only return true if distance is at least 1 less than last
     * @param distance
     * @return
     */
    public boolean distanceImproved(double distance) {
        if (!has(last_trigger_distance)){
            return true;
        }
        return false;
//        return (getDouble(last_trigger_distance) - distance) >= 0.5;
    }

    public void setLastDistance(double distance) {
        put(last_trigger_distance, distance);
    }

    public void reset() {
        remove(last_trigger_distance);
        remove(last_trigger_probability);
        setChecked(false);
    }

    public void setLastProbability(double probability) {
        put(last_trigger_probability, probability);
    }

    public double getLastProbability() {
        return getDouble(last_trigger_probability);
    }

    public double getLastDistance() {
        return getDouble(last_trigger_distance);
    }

    public boolean isChecked() {
        return getBoolean(isChecked);
    }

    public void setChecked(boolean checked) {
        put(isChecked, checked);
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


    public boolean hasFingerprint() {
        return has(fingerprint);
    }

    public void removeFingerprint() {
        remove(fingerprint);
    }


}
