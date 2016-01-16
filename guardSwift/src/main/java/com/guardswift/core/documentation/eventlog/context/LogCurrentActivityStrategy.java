package com.guardswift.core.documentation.eventlog.context;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogCurrentActivityStrategy implements LogContextStrategy {

    public static final String activityType = "activityType";
    public static final String activityConfidence = "activityConfidence";
    public static final String activityName = "activityName";

    @Override
    public void log(ParseObject toParseObject) {

        DetectedActivity detectedActivity = ActivityDetectionModule.Recent.getDetectedActivity();

        if (detectedActivity != null) {
            toParseObject.put(LogCurrentActivityStrategy.activityType, detectedActivity.getType());
            toParseObject.put(LogCurrentActivityStrategy.activityConfidence, detectedActivity.getConfidence());
            toParseObject.put(LogCurrentActivityStrategy.activityName, ActivityDetectionModule.getNameFromType(detectedActivity.getType()));
        }
    }
}
