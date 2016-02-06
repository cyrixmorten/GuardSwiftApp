package com.guardswift.core.documentation.report;

import android.content.Context;
import android.util.Log;

import com.guardswift.core.documentation.eventlog.context.LogStrategyFactory;
import com.guardswift.core.documentation.eventlog.task.TaskLogStrategyFactory;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.parse.ParseException;
import com.parse.SaveCallback;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by cyrix on 6/8/15.
 */
public class StandardTaskReportingStrategy<T extends BaseTask> implements TaskReportingStrategy<T> {

    private static final String TAG = StandardTaskReportingStrategy.class.getSimpleName();

    private final T task;

    public StandardTaskReportingStrategy(T task) {
        this.task = task;
    }

    public Task<Report> getReport() {
        Log.w(TAG, "getReport");
        return prepareReport(false);
    };

    private Task<Report> prepareReport(boolean queryOnline) {

        final boolean useLocalDatastore = !queryOnline;
        return Report.getQueryBuilder(useLocalDatastore).matching(task).build().getFirstInBackground().continueWithTask(new Continuation<Report, Task<Report>>() {
            @Override
            public Task<Report> then(Task<Report> reportTask) throws Exception {
                if (reportTask.isFaulted()) {
                    Exception error = reportTask.getError();
                    if (error instanceof ParseException && ((ParseException) error).getCode() != ParseException.OBJECT_NOT_FOUND) {
                        // it is expected that new reports are not found, any other errors, however, are reported
                        new HandleException(TAG, "Prepare standard report", reportTask.getError());
                        return reportTask;
                    } else {
                        if (useLocalDatastore) {
                            Log.w(TAG, "Report not found locally - attempt online");
                            return prepareReport(true);
                        }
                    }
                }
                Report report = reportTask.getResult();
                if (report == null) {
                    // Report not found
                    Log.w(TAG, "No report found - creating new for " + task.getClient().getName());
                    report = Report.create(new LogStrategyFactory(), new TaskLogStrategyFactory(), task);

                }
                return Task.forResult(report);
            }
        });
    }

    @Override
    public void addUnique(final Context context, final EventLog eventLog, final SaveCallback saveCallback) {
        Log.w(TAG, "Adding eventlog to report");
        prepareReport(false).onSuccess(new Continuation<Report, Object>() {
            @Override
            public Object then(Task<Report> reportTask) throws Exception {
                // Report found or created
                Report report = reportTask.getResult();

//                    Log.w(TAG, "Adding JSON event to report");
//                    report.addEntry(eventLog.asJSONObject());
                report.add(eventLog);

                if (eventLog.getEventCode() == EventLog.EventCodes.CIRCUITUNIT_EXTRA_TIME) {
                    report.extraTimeSpent(eventLog.getAmount());
                }


                Log.w(TAG, "Saving report for task at client: " + task.getClient().getName());
                report.pinThenSaveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.w(TAG, "Pinned " + e);
                    }
                }, saveCallback);

                return null;
            }
        });
        ;
    }

    @Override
    public void remove(Context context, final EventLog eventLog, final SaveCallback saveCallback) {
        prepareReport(false).onSuccess(new Continuation<Report, Object>() {
            @Override
            public Object then(Task<Report> reportTask) throws Exception {
                // Report found or created
                Report report = reportTask.getResult();

//                    Log.w(TAG, "Adding JSON event to report");
//                    report.addEntry(eventLog.asJSONObject());
                report.remove(eventLog);

                report.pinThenSaveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.w(TAG, "Pinned " + e);
                    }
                }, saveCallback);

                return null;
            }
        });
    }


}
