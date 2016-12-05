package com.guardswift.persistence.parse.execution.task.districtwatch;

import android.content.Context;
import android.util.Log;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import bolts.Task;

@ParseClassName("DistrictWatchStarted")
public class DistrictWatchStarted extends ExtendedParseObject {

//    public static class Recent {
//
//        private static String TAG = "DistrictWatchStarted.Recent";
//
//        private static DistrictWatchStarted selected;
//
//        public static void setSelected(ParseModulePreferences preferences, DistrictWatchStarted DistrictWatchStarted) {
//
//            selected = DistrictWatchStarted;
//
//            if (preferences == null)
//                return;
//
//            String objectId = DistrictWatchStarted.getObjectId();
//            if (objectId != null) {
//                Log.d(TAG, "setDistrictWatchStarted " + objectId);
//                preferences.setDistrictWatchStartedObjectId(objectId);
//            } else {
//                throw new NullPointerException("DistrictWatchStarted has no objectId");
//            }
//
//            DistrictWatch.Recent.setSelected(preferences, DistrictWatchStarted.getDistrictWatch());
//        }
//
//        public static DistrictWatchStarted getSelected() {
//            return selected;
//        }
//
//        public static DistrictWatchStarted getSelected(ParseModulePreferences preferences) {
//
//            if (selected != null)
//                return selected;
//
//
//            if (preferences.isGuardLoggedIn()
//                    && preferences.isCircuitSelected()) {
//                String objectId = preferences.getDistrictWatchStartedObjectId();
//                try {
//                    selected = Query.get(objectId);
//                } catch (ParseException e) {
//                    Log.e(TAG, "getCurrentDistrictwatchStarted not found");
//                }
//
//            } else {
//                // No DistrictWatchStarted Selected
//            }
//
//            return selected;
//        }
//
//    }

    public static class Query {

        private static String TAG = "DistrictWatch.Query";

        /*
 * Get newest circuitStarted based on circuit
 */
        public static Task<DistrictWatchStarted> findInBackground(DistrictWatch districtWatch) {
            ParseQuery<DistrictWatchStarted> query = new DistrictWatchStarted.QueryBuilder(true)
                    .matching(districtWatch).whereActive().build();
            return query.getFirstInBackground();
        }


		/*
 * Get newest DistrictWatchStarted based on DistrictWatch
 */
		public static DistrictWatchStarted findFrom(DistrictWatch districtWatch) throws ParseException {
			ParseQuery<DistrictWatchStarted> query = new DistrictWatchStarted.QueryBuilder(true)
					.matching(districtWatch).whereActive().build();
			return query.getFirst();
		}

        public static DistrictWatchStarted get(String objectId)
                throws ParseException {
            Log.e(TAG, "getDistrictWatchStarted: " + objectId);
            ParseQuery<DistrictWatchStarted> query = new DistrictWatchStarted.QueryBuilder(true)
                    .matchingObjectId(objectId).build();
            return query.getFirst();
        }

        public static boolean isDistrictWatchesAvailable() {
            try {
                int count = DistrictWatchStarted.getQueryBuilder(true).build().count();
                Log.d(TAG, "isDistrictWatchesAvailable count: " + count);
                return count != 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }


	}
    

	public static final String name = "name";
	public static final String districtWatch = "districtWatch";
	public static final String timeStarted = "timeStarted";
	public static final String timeEnded = "timeEnded";

	@Override
	public String getParseClassName() {
		return DistrictWatchStarted.class.getSimpleName();
	}

	public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
		return new QueryBuilder(fromLocalDatastore);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<DistrictWatchStarted> getAllNetworkQuery() {
		return new QueryBuilder(false).build();
	}

	@Override
	public void updateFromJSON(final Context context,
			final JSONObject jsonObject) {
		// TODO Auto-generated method stub
	}

	public static class QueryBuilder extends
			ParseQueryBuilder<DistrictWatchStarted> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
					.getQuery(DistrictWatchStarted.class));
		}

		@Override
		public ParseQuery<DistrictWatchStarted> build() {
			query.include(districtWatch);
			whereActive();
			return super.build();
		}

		public QueryBuilder whereActive() {
			query.whereDoesNotExist(CircuitStarted.timeEnded);
			return this;
		}

		public QueryBuilder sortByName() {
			query.addAscendingOrder(name);
			return this;
		}

		public QueryBuilder matching(DistrictWatch districtWatch) {
			query.whereEqualTo(DistrictWatchStarted.districtWatch,
					districtWatch);
			return this;
		}

	}

	public GSTask.TASK_TYPE getTaskType() {
		return GSTask.TASK_TYPE.DISTRICTWATCH;
	}

	public String getName() {
		return getString(name);
	}

	public ParseObject getOwner() {
		return getParseObject(owner);
	}

	public String getTimeStarted() {
		return getString(timeStarted);
	}

	public String getTimeEnded() {
		return getString(timeEnded);
	}

	public DistrictWatch getDistrictWatch() {
        if (getParseObject(districtWatch) == null) {
            Log.e(TAG, "getDistrictWatch null for: " + getName() + " - " + getObjectId());
        }
		return (DistrictWatch) getParseObject(districtWatch);
	}
}
