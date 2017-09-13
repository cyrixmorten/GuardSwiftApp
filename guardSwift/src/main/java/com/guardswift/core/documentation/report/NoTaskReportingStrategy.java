//package com.guardswift.core.documentation.report;
//
//import android.util.Log;
//
//import com.guardswift.core.exceptions.HandleException;
//import com.guardswift.persistence.parse.documentation.report.Report;
//import com.guardswift.persistence.parse.execution.task.ParseTask;
//import com.parse.ParseException;
//
//import bolts.Continuation;
//import bolts.Task;
//
//public class NoTaskReportingStrategy implements TaskReportingStrategy {
//
//    private static final String TAG = NoTaskReportingStrategy.class.getSimpleName();
//
//    private final ParseTask task;
//
//    public NoTaskReportingStrategy(ParseTask task) {
//        this.task = task;
//    }
//
//    private Task<Report> findReport(final boolean fromLocalDataStore) {
//        return Report.getQueryBuilder(fromLocalDataStore).matching(task).build().getFirstInBackground().continueWithTask(new Continuation<Report, Task<Report>>() {
//            @Override
//            public Task<Report> then(Task<Report> reportTask) throws Exception {
//                if (reportTask.isFaulted()) {
//                    Exception error = reportTask.getError();
//                    if (error instanceof ParseException && ((ParseException) error).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                        // it is expected that new reports are not found, any other errors, however, are reported
//                        new HandleException(TAG, "FindReport", error);
//
//                        return reportTask;
//                    } else {
//                        if (fromLocalDataStore) {
//                            Log.w(TAG, "Report not found locally - attempt online");
//                            return findReport(false);
//                        } else {
//
//                            new HandleException(TAG, "Not able to find report", error);
//
//                            throw error;
//                        }
//                    }
//                }
//                Report report = reportTask.getResult();
//
//                // successfully located report
//                // store in LDS if found online
//                if (!fromLocalDataStore) {
//                    report.pinInBackground();
//                }
//
//                return Task.forResult(report);
//            }
//        });
//    }
//
//    @Override
//    public Task<Report> getReport() {
//        return findReport(true);
//    }
//
//
//}
