package com.guardswift.persistence.parse.misc;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.util.Device;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

@ParseClassName("Update")
public class Update extends ExtendedParseObject {


    public static final String versionNumber = "versionNumber";
    public static final String versionName = "versionName";
    public static final String androidApk = "androidApk";




    @Override
    public String getParseClassName() {
        return Update.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Update> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore, String groupId) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public static class QueryBuilder extends ParseQueryBuilder<Update> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Update.class), true);
        }


        @Override
        public ParseQuery<Update> build() {
            return super.build();
        }


    }

    public int getVersionNumber() {
        return getInt(Update.versionNumber);
    }

    public String getVersionName() {
        return getStringSafe(Update.versionName);
    }

    public boolean isNewerThanInstalled() {
        int currentVersion = new Device().getVersionCode();
        return currentVersion < getVersionNumber();
    }

    public ParseFile getUpdateFile() {
         return getParseFile(Update.androidApk);
    }

}
