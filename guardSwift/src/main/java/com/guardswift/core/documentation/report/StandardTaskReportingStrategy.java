package com.guardswift.core.documentation.report;

import android.content.Context;
import android.util.Log;

import com.guardswift.core.documentation.eventlog.context.LogStrategyFactory;
import com.guardswift.core.documentation.eventlog.context.TaskLogStrategyFactory;
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

    private Task<Report> prepareReport(final Context context) {
        return Report.getQueryBuilder(true).matching(task).build().getFirstInBackground().continueWithTask(new Continuation<Report, Task<Report>>() {
            @Override
            public Task<Report> then(Task<Report> reportTask) throws Exception {
                Report report = reportTask.getResult();
                if (reportTask.isFaulted() || report == null) {
                    // Report not found
                    Log.w(TAG, "No report found - creating new for " + task.getClient().getName());
                    report = Report.create(new LogStrategyFactory(context), new TaskLogStrategyFactory(), task);
                }
                return Task.forResult(report);
            }
        });
    }

    @Override
    public void addUnique(final Context context, final EventLog eventLog, final SaveCallback saveCallback) {
        Log.w(TAG, "Adding eventlog to report");
        prepareReport(context).onSuccess(new Continuation<Report, Object>() {
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
        prepareReport(context).onSuccess(new Continuation<Report, Object>() {
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
