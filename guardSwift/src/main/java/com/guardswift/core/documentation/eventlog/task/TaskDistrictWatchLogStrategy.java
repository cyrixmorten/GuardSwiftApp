//package com.guardswift.core.documentation.eventlog.task;
//
//import com.guardswift.persistence.parse.execution.ParseTask;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatch;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
//import com.parse.ParseObject;
//
///**
// * Created by cyrix on 6/7/15.
// */
//public class TaskDistrictWatchLogStrategy implements LogTaskStrategy {
//
//
//    public static final String districtWatchStarted = "districtWatchStarted";
//    public static final String districtWatchClient = "districtWatchClient";
//    public static final String timeStart = "timeStart";
//    public static final String timeStartString = "timeStartString";
//    public static final String timeEnd = "timeEnd";
//    public static final String timeEndString = "timeEndString";
//
//
//    @Override
//    public void log(ParseTask task, ParseObject toParseObject) {
//        if (task instanceof DistrictWatchClient) {
//
//            DistrictWatchClient districtWatchClient = (DistrictWatchClient)task;
//
//            toParseObject.put(TaskDistrictWatchLogStrategy.districtWatchClient, districtWatchClient);
//
//            DistrictWatchStarted  districtWatchStarted = districtWatchClient.getDistrictWatchStarted();
//
//            setDistrictWatchStarted(districtWatchStarted, toParseObject);
//
//        }
//
//    }
//
//    private void setDistrictWatchStarted(
//            DistrictWatchStarted districtWatchStarted, ParseObject toParseObject) {
//
//        if (districtWatchStarted != null) {
//            toParseObject.put(TaskDistrictWatchLogStrategy.districtWatchStarted, districtWatchStarted);
//
//            DistrictWatch districtWatch = districtWatchStarted.getDistrictWatch();
//            if (districtWatch != null) {
//                toParseObject.put(TaskDistrictWatchLogStrategy.timeStart, districtWatch.getTimeStart());
//                toParseObject.put(TaskDistrictWatchLogStrategy.timeStartString, districtWatch.getTimeStartString());
//                toParseObject.put(TaskDistrictWatchLogStrategy.timeEnd, districtWatch.getTimeEnd());
//                toParseObject.put(TaskDistrictWatchLogStrategy.timeEndString, districtWatch.getTimeEndString());
//            }
//
//        }
//
//    }
//}
