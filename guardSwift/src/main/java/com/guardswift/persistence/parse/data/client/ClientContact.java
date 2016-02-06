package com.guardswift.persistence.parse.data.client;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

@ParseClassName("ClientContact")
public class ClientContact extends ExtendedParseObject {


	public static final String name = "name";
	public static final String desc = "desc";
	public static final String phoneNumber = "phoneNumber";
	public static final String email = "email";
	public static final String receiveReports = "receiveReports";

	@Override
	public String getParseClassName() {
		return ClientContact.class.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<ClientContact> getAllNetworkQuery() {
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

	public String getEmail() {
		return (has(ClientContact.email)) ? getString(ClientContact.email) : "";
	}

	public boolean isReceivingReports() {
		return getBoolean(receiveReports);
	}

	public static class QueryBuilder extends ParseQueryBuilder<ClientContact> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
					.getQuery(ClientContact.class));
		}

	}

	public String getName() {
        return (has(name)) ? getString(name) : "";
    }

    public String getDesc() {
        return (has(desc)) ? getString(desc) : "";
    }

    public String getPhoneNumber() {
        return (has(phoneNumber)) ? getString(phoneNumber) : "";
    }

}
