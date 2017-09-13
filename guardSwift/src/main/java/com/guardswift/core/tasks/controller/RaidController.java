package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;
import com.parse.ParseException;
import com.parse.SaveCallback;

public class RaidController extends BaseTaskController {


    private static final String TAG = RaidController.class.getSimpleName();


    private static RaidController instance;
    public static RaidController getInstance() {
        if (instance == null) {
            instance = new RaidController();
        }

        return instance;
    }

    private RaidController() {}

    public ParseTask performAction(TaskController.ACTION action, final ParseTask task, final boolean automatic) {

        Context ctx = GuardSwiftApplication.getInstance();
        ParseTasksCache tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();


        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, districtWatchClient);
                break;
            case ARRIVE:


                task.setArrived();

                new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .location(task.getFullAddress())
                        .eventCode(EventLog.EventCodes.RAID_ARRIVED).saveAsync();

                tasksCache.addArrived(task);
                break;
            case RESET:

                tasksCache.removeArrived(task);
                task.reset();

                break;
            case OPEN_WRITE_REPORT:
                ParseTaskCreateReportActivity.start(ctx, task);
                break;

            default:
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return task;

        }


        task.pinThenSaveEventually((new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (automatic) {
                    EventBusController.postUIUpdate(task);
                }
            }
        }));

        return task;
    }


}
