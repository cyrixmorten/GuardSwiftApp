//package com.guardswift.persistence.parse.execution.task.districtwatch;
//
//import android.content.Context;
//
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.ParseQueryBuilder;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.parse.ParseClassName;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;
//
//import org.json.JSONObject;
//
//@ParseClassName("DistrictWatchUnit")
//public class DistrictWatchUnit extends ExtendedParseObject {
//
//
//	public static final String objectName = "CircuitUnit";
//	public static final String type = "type";
//	public static final String client = "client";
//	public static final String districtWatch = "districtWatch";
//
//	@Override
//	public String getParseClassName() {
//		return DistrictWatchUnit.class.getSimpleName();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public ParseQuery<DistrictWatchUnit> getAllNetworkQuery() {
//		return new QueryBuilder(false).build();
//	}
//
//	@Override
//	public void updateFromJSON(final Context context,
//			final JSONObject jsonObject) {
//		// TODO Auto-generated method stub
//	}
//
//	public static class QueryBuilder extends
//			ParseQueryBuilder<DistrictWatchUnit> {
//
//		public QueryBuilder(boolean fromLocalDatastore) {
//			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
//					.getQuery(DistrictWatchUnit.class));
//		}
//
//		@Override
//		public ParseQuery<DistrictWatchUnit> build() {
//			query.include(client);
//			query.setLimit(1000);
//			return super.build();
//		}
//
//	}
//
//	public DistrictWatch getDistrictWatch() {
//		return (DistrictWatch) getParseObject(districtWatch);
//	}
//
//	public String getType() {
//		return getString(type);
//	}
//
//	public ParseObject getOwner() {
//		return getParseObject(owner);
//	}
//
//	public Client getClient() {
//		return (Client) getParseObject(client);
//	}
//
//	// public DistrictWatch getDistrictWatch() {
//	// return (DistrictWatch)getParseObject(districtWatch);
//	// }
//
//}
