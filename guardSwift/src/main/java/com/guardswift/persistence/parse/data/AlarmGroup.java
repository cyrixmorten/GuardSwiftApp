package com.guardswift.persistence.parse.data;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

import org.json.JSONObject;

@ParseClassName("AlarmGroup")
public class AlarmGroup extends ExtendedParseObject {


	public static final String PIN = "AlarmGroup";

	public static final String name = "name";
	public static final String responsible = "responsible";


	@Override
	public String getPin() {
		return PIN;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<AlarmGroup> getAllNetworkQuery() {
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

	public static class QueryBuilder extends ParseQueryBuilder<AlarmGroup> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(PIN, fromLocalDatastore, ParseQuery.getQuery(AlarmGroup.class));
		}

		public QueryBuilder sortByName() {
			query.addAscendingOrder(name);
			return this;
		}

	}

	public String getName() {
		return getString(name);
	}

    public boolean isResponsible() {
        return getBoolean(AlarmGroup.responsible);
    }

    public void setResponsible(boolean responsible) {
        put(AlarmGroup.responsible, responsible);
    }

}
