package com.guardswift.core.documentation.report;

import android.util.Log;

import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseException;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by cyrix on 6/8/15.
 */
public class NoTaskReportingStrategy implements TaskReportingStrategy {

    private static final String TAG = NoTaskReportingStrategy.class.getSimpleName();

    private final GSTask task;

    public NoTaskReportingStrategy(GSTask task) {
        this.task = task;
    }

    private Task<Report> findReport(final boolean fromLocalDataStore) {
        return Report.getQueryBuilder(fromLocalDataStore).matching(task).build().getFirstInBackground().continueWithTask(new Continuation<Report, Task<Report>>() {
            @Override
            public Task<Report> then(Task<Report> reportTask) throws Exception {
                if (reportTask.isFaulted()) {
                    Exception error = reportTask.getError();
                    if (error instanceof ParseException && ((ParseException) error).getCode() != ParseException.OBJECT_NOT_FOUND) {
                        // it is expected that new reports are not found, any other errors, however, are reported
                        new HandleException(TAG, "FindReport", error);

                        return reportTask;
                    } else {
                        if (fromLocalDataStore) {
                            Log.w(TAG, "Report not found locally - attempt online");
                            return findReport(false);
                        } else {

                            new HandleException(TAG, "Not able to find report", error);

                            throw error;
                        }
                    }
                }
                Report report = reportTask.getResult();

                // successfully located report
                // store in LDS if found online
                if (!fromLocalDataStore) {
                    report.pinInBackground();
                }

                return Task.forResult(report);
            }
        });
    }

    @Override
    public Task<Report> getReport() {
        return findReport(true);
    }
//
//    @Override
//    public void addUnique(Context context, final EventLog eventLog, SaveCallback saveCallback) {
//        // do nothing        ;
//    }
//
//    @Override
//    public void remove(Context context, final EventLog eventLog, final SaveCallback saveCallback) {
//        findReport(false).onSuccess(new Continuation<Report, Object>() {
//            @Override
//            public Object then(Task<Report> reportTask) throws Exception {
//                // Report found or created
//                Report report = reportTask.getResult();
//
//                report.remove(eventLog);
//
//                report.pinThenSaveEventually(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        Log.w(TAG, "Pinned " + e);
//                    }
//                }, saveCallback);
//
//                return null;
//            }
//        });
//    }
}
