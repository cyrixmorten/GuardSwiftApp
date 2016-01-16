package com.guardswift.core.documentation.eventlog.context;

import com.parse.ParseObject;

/**
 * Created by cyrix on 6/7/15.
 */
public interface LogContextStrategy {

    void log(ParseObject toParseObject);

}
