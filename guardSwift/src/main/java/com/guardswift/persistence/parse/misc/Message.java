package com.guardswift.persistence.parse.misc;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.query.MessageQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

import java.util.Date;

@ParseClassName("Message")
public class Message extends ExtendedParseObject {


    public static final String guard = "guard";
    private static final String message = "message";
    private static final String deviceTimestamp = "deviceTimestamp";
    public static final String groupId = "groupId";

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


    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Message> getAllNetworkQuery() {
        return new MessageQueryBuilder(false).build();
    }


    public static MessageQueryBuilder getQueryBuilder(boolean fromLocalDatastore, String groupId) {
        MessageQueryBuilder builder =  new MessageQueryBuilder(fromLocalDatastore);
        builder.withGroupId(groupId);
        return builder;
    }




}
