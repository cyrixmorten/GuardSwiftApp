package com.guardswift.core.documentation.eventlog.context;

import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogTimestampStrategy implements LogContextStrategy {

    public static final String deviceTimestamp = "deviceTimestamp";

    @Override
    public void log(ParseObject toParseObject) {

        toParseObject.put(LogTimestampStrategy.deviceTimestamp, new Date());
    }
}
