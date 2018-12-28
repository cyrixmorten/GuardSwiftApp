package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.fabric.TrackEvent;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;

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

        TrackEvent.taskAction(action, task, automatic);

        Context ctx = GuardSwiftApplication.getInstance();
        ParseTasksCache tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();

        EventLog.Builder event = null;

        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, districtWatchClient);
                break;
            case ARRIVE:


                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .location(task.getFullAddress())
                        .eventCode(EventLog.EventCodes.RAID_ARRIVED);

                if (automatic) {
                    task.setArrived();
                    tasksCache.addArrived(task);
                }

                task.incrementArrivedCount();

                break;
            case FINISH:


                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_FINISHED);


                task.setFinished();
                tasksCache.removeArrived(task);

                break;
            case PENDING:

                tasksCache.removeArrived(task);
                task.setPending();

                break;
            case OPEN_WRITE_REPORT:
                ParseTaskCreateReportActivity.start(ctx, task);
                break;

            default:
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return task;

        }


        task.saveEventuallyAndNotify();

        if (event != null) {
            event.saveAsync();
        }

        return task;
    }


}
