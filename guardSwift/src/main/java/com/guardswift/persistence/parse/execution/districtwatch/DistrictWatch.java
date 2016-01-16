package com.guardswift.persistence.parse.execution.districtwatch;

import android.content.Context;
import android.util.Log;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.util.Date;

@ParseClassName("DistrictWatch")
public class DistrictWatch extends ExtendedParseObject {

//    public static class Recent {
//
//        private static String TAG = "DistrictWatch.Recent";
//
//        private static DistrictWatch selected;
//
//
//        public static void setSelected(ParseModulePreferences preferences, DistrictWatch DistrictWatch) {
//
//            selected = DistrictWatch;
//
//            if (preferences == null)
//                return;
//
//            String objectId = DistrictWatch.getObjectId();
//            if (objectId != null) {
//                Log.d(TAG, "setDistrictWatch " + objectId);
//                preferences.setDistrictWatchObjectId(objectId);
//            } else {
//                throw new NullPointerException("DistrictWatch has no objectId");
//            }
//        }
//
//        public static DistrictWatch getSelected() {
//            return selected;
//        }
//
//        public static DistrictWatch getSelected(ParseModulePreferences preferences) {
//
//            if (selected != null)
//                return selected;
//
//
//            if (preferences.isGuardLoggedIn()
//                    && preferences.isDistrictWatchSelected()) {
//                String objectId = preferences.getDistrictWatchObjectId();
//                try {
//                    selected = Query.get(objectId);
//                } catch (ParseException e) {
//                    Log.e(TAG, "getCurrentDistrictWatch not found");
//                }
//
//            } else {
//                // No DistrictWatch Selected
//            }
//
//            return selected;
//        }
//
//    }

    public static class Query {

        private static String TAG = "DistrictWatch.Query";

        public static DistrictWatch get(String objectId)
                throws ParseException {
            Log.e(TAG, "getDistrictWatchStarted: " + objectId);
            ParseQuery<DistrictWatch> query = new DistrictWatch.QueryBuilder(true)
                    .matchingObjectId(objectId).build();
            return query.getFirst();
        }
    }
    
	public static final String PIN = "DistrictWatch";

	public static final String name = "name";
	public static final String city = "city";
	public static final String zipcode = "zipcode";
    public static final String timeStartDate = "timeStartDate";
    public static final String timeEndDate = "timeEndDate";

	@Override
	public String getPin() {
		return PIN;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<DistrictWatch> getAllNetworkQuery() {
		return new QueryBuilder(false).build();
	}

	@Override
	public void updateFromJSON(final Context context,
			final JSONObject jsonObject) {
		// TODO Auto-generated method stub
	}

	public static class QueryBuilder extends ParseQueryBuilder<DistrictWatch> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(PIN, fromLocalDatastore, ParseQuery
					.getQuery(DistrictWatch.class));
		}

	}

    public Date getTimeStart() {
        return getDate(timeStartDate);
    }

    public String getTimeStartString() {
        Date date = getDate(timeStartDate);
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

    public Date getTimeEnd() {
        return getDate(timeEndDate);
    }

    public String getTimeEndString() {
        Date date = getDate(timeEndDate);
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

	public String getName() {
		return getString(name);
	}

	public ParseObject getOwner() {
		return getParseObject(owner);
	}

	public String getCity() {
		return getString(city);
	}

	public String getZipcode() {
		return getString(zipcode);
	}

}
