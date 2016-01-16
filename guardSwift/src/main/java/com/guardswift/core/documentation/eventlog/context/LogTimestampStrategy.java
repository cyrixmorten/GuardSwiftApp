package com.guardswift.core.documentation.eventlog.context;

import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogTimestampStrategy implements LogContextStrategy {

    public static final String deviceTimestamp = "deviceTimestamp";
    public static final String clientTimestamp = "clientTimestamp"; // TODO deprecate in favor of deviceTimeStamp

    @Override
    public void log(ParseObject toParseObject) {

        toParseObject.put(LogTimestampStrategy.clientTimestamp, new Date()); // TODO deprecate
        toParseObject.put(LogTimestampStrategy.deviceTimestamp, new Date());
    }
}
