package com.guardswift.persistence.parse.query;


import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.misc.Message;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class MessageQueryBuilder extends
        ParseQueryBuilder<Message> {

    public MessageQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(Message.class));
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
