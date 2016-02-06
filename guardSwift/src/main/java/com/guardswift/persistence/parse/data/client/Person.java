package com.guardswift.persistence.parse.data.client;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

@ParseClassName("Person")
public class Person extends ExtendedParseObject implements Comparable<Person> {


	public static final String name = "name";
	public static final String desc = "desc";
	public static final String phoneNumber = "phoneNumber";

	public static Person create(String name) {
		Person person = new Person();
		person.setName(name);
		return person;
	}

	@Override
	public String getParseClassName() {
		return Person.class.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<Person> getAllNetworkQuery() {
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

	@Override
	public int compareTo(Person person) {
		return getName().compareTo(person.getName());
	}

	public static class QueryBuilder extends ParseQueryBuilder<Person> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
					.getQuery(Person.class));
		}

	}

	public void setName(String name) {
		put(Person.name, name);
	}

	public String getName() {
        return getString(name);
    }

    public String getDesc() {
        return getString(desc);
    }

    public String getPhoneNumber() {
        return getString(phoneNumber);
    }

}
