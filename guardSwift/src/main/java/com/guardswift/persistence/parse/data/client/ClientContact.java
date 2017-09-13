package com.guardswift.persistence.parse.data.client;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
