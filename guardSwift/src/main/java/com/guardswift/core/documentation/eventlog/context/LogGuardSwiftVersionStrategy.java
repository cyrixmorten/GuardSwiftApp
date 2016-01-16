package com.guardswift.core.documentation.eventlog.context;

import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Device;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogGuardSwiftVersionStrategy implements LogContextStrategy {

    public static final String gsVersion = "gsVersion";

    @Override
    public void log(ParseObject toParseObject) {

        toParseObject.put(LogGuardSwiftVersionStrategy.gsVersion, new Device(GuardSwiftApplication.getInstance()).getVersionCode());
    }
}
