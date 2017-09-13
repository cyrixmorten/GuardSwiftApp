//package com.guardswift.persistence.parse.execution.task.regular;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.ParseQueryBuilder;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatch;
//import com.parse.ParseClassName;
//import com.parse.ParseException;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;
//
//import org.json.JSONObject;
//
//import java.util.List;
//
//@ParseClassName("Circuit")
//public class Circuit extends ExtendedParseObject {
//
////    public static class Recent {
////
////        private static String TAG = "Circuit.Recent";
////
////        private static Circuit selected;
////
////
////        public static void setSelected(ParseModulePreferences preferences, Circuit taskGroup) {
////
////            selected = taskGroup;
////
////            if (preferences == null)
////                return;
////
////            String objectId = taskGroup.getObjectId();
////            if (objectId != null) {
////                Log.d(TAG, "setCircuit " + objectId);
////                preferences.setCircuitObjectId(objectId);
////            } else {
////                throw new NullPointerException("Circuit has no objectId");
////            }
////        }
////
////        public static Circuit getSelected() {
////            return selected;
////        }
////
////        public static Circuit getSelected(ParseModulePreferences preferences) {
////
////            if (selected != null)
////                return selected;
////
////
////            if (preferences.isGuardLoggedIn()
////                    ) {
////                String objectId = preferences.getCircuitObjectId();
////                try {
////                    selected = Query.get(objectId);
////                } catch (ParseException e) {
////                    Log.e(TAG, "getCurrentCircuit not found");
////                }
////
////            }
////
////            return selected;
////        }
////
////    }
//
////    public static class Query {
////
////        private static String TAG = "Circuit.Query";
////
////        public static Circuit get(String objectId)
////                throws ParseException {
////            Log.e(TAG, "getCircuit: " + objectId);
////            ParseQuery<Circuit> query = new Circuit.QueryBuilder(true)
////                    .matchingObjectId(objectId).build();
////            return query.getFirst();
////        }
////    }
//
//
////	public static final String objectName = "Circuit";
//	public static final String name = "name";
////	public static final String districtWatches = "districtWatches";
////	public static final String timeStart = "timeStart";
////	public static final String timeEnd = "timeEnd";
//
////	private static final String owner = "owner";
//
//	@Override
//	public String getParseClassName() {
//		return Circuit.class.getSimpleName();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public ParseQuery<Circuit> getAllNetworkQuery() {
//		return new QueryBuilder(false).build();
//	}
//
//	@Override
//	public void updateFromJSON(final Context context,
//			final JSONObject jsonObject) {
//		// TODO Auto-generated method stub
//	}
//
//	public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
//		return new QueryBuilder(fromLocalDatastore);
//	}
//
//	public static class QueryBuilder extends ParseQueryBuilder<Circuit> {
//
//		public QueryBuilder(boolean fromLocalDatastore) {
//			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Circuit.class));
//		}
//
//		public QueryBuilder sortByName() {
//			query.addAscendingOrder(name);
//			return this;
//		}
//
//	}
//
//	public String getName() {
//		return getString(name);
//	}
//
//	public List<DistrictWatch> getDistrictWatches() {
//		return getList(districtWatches);
//	}
//
//	public ParseObject getOwner() {
//		return getParseObject(owner);
//	}
//
////	public String getTimeStart() {
////		return getString(timeStart);
////	}
////
////	public String getTimeEnd() {
////		return getString(timeEnd);
////	}
//}
