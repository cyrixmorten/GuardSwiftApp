//package com.guardswift.persistence.parse.data.checklist;
//
//import android.content.Context;
//
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.ParseQueryBuilder;
//import com.parse.ParseClassName;
//import com.parse.ParseQuery;
//
//import org.json.JSONObject;
//
//@ParseClassName("ChecklistCircuitEnding")
//public class ChecklistCircuitEnding extends ExtendedParseObject {
//
//	public static final String PIN = "ChecklistCircuitEnding";
//
//	public static final String item = "item";
//
//	@Override
//	public String getPin() {
//		return PIN;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public ParseQuery<ChecklistCircuitEnding> getAllNetworkQuery() {
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
//			ParseQueryBuilder<ChecklistCircuitEnding> {
//
//		public QueryBuilder(boolean fromLocalDatastore) {
//			super(PIN, fromLocalDatastore, ParseQuery
//					.getQuery(ChecklistCircuitEnding.class));
//		}
//
//	}
//
//	public String getItem() {
//		return getString(item);
//	}
//
//}
