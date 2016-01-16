package com.guardswift.persistence.parse.data.checklist;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

import org.json.JSONObject;

@ParseClassName("ChecklistCircuitStarting")
public class ChecklistCircuitStarting extends ExtendedParseObject {

	public static final String PIN = "ChecklistCircuitStarting";

	public static final String item = "item";

	@Override
	public String getPin() {
		return PIN;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<ChecklistCircuitStarting> getAllNetworkQuery() {
		return new QueryBuilder(false).build();
	}

	@Override
	public void updateFromJSON(final Context context,
			final JSONObject jsonObject) {
		// TODO Auto-generated method stub
	}

	public static class QueryBuilder extends
			ParseQueryBuilder<ChecklistCircuitStarting> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(PIN, fromLocalDatastore, ParseQuery
					.getQuery(ChecklistCircuitStarting.class));
		}

	}

	public String getItem() {
		return getString(item);
	}

}
