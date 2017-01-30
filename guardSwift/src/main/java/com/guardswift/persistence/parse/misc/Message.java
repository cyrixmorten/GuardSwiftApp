package com.guardswift.persistence.parse.misc;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.Date;

@ParseClassName("Message")
public class Message extends ExtendedParseObject {


    private static final String guard = "guard";
    private static final String message = "message";
    private static final String deviceTimestamp = "deviceTimestamp";
    private static final String groupId = "groupId";

    public static Message newInstance(String groupId, String messageBody) {

        Message message = new Message();
        message.setDefaultOwner();
        message.put(Message.deviceTimestamp, new Date());
        message.put(Message.groupId, groupId);
        message.put(Message.guard, GuardSwiftApplication.getLoggedIn());
        message.put(Message.message, messageBody);

        return message;
    }

    public void setMessage(String message) {
        put(Message.message, message);
    }

    public String getMessage() {
        return getStringSafe(Message.message);
    }

    public Guard getGuard() {
        return (Guard) getLDSFallbackParseObject(Message.guard);
    }

    @Override
    public String getParseClassName() {
        return Message.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Message> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore, String groupId) {
        QueryBuilder builder =  new QueryBuilder(fromLocalDatastore);
        builder.withGroupId(groupId);
        return builder;
    }

    public static class QueryBuilder extends ParseQueryBuilder<Message> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Message.class));
        }

        public ParseQuery<Message> withGroupId(String groupId) {
            query.whereEqualTo(Message.groupId, groupId);
            return super.build();
        }

        @Override
        public ParseQuery<Message> build() {
            query.include(Message.guard);
            return super.build();
        }


    }


}
