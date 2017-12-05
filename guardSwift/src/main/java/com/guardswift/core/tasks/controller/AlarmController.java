package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.fabric.TrackEvent;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;

public class AlarmController extends BaseTaskController {


    private static final String TAG = AlarmController.class.getSimpleName();


    private static AlarmController instance;
    public static AlarmController getInstance() {
        if (instance == null) {
            instance = new AlarmController();
        }

        return instance;
    }

    private AlarmController() {}


    public ParseTask performAction(ACTION action, ParseTask alarm, boolean automatic) {

        TrackEvent.taskAction(action, alarm, automatic);

        Context ctx = GuardSwiftApplication.getInstance();

        EventLog.Builder event = null;

        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, parseObject);
                break;
            case FORWARD:

//                event = new EventLog.Builder(ctx)
//                        .taskPointer(alarm, ParseTask.EVENT_TYPE.ACCEPT)
//                        .event(ctx.getString(R.string.event_forwarded))
//                        .remarks(ctx.getString(R.string.alarm_group) + " " + alarm.getForwardedTo())
//                        .automatic(automatic)
//                        .eventCode(EventLog.EventCodes.ALARM_FORWARDED);

                break;
            case ACCEPT:

                alarm.setAccepted();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, ParseTask.EVENT_TYPE.ACCEPT)
                        .event(ctx.getString(R.string.event_accepted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ACCEPTED);

                break;
            case IGNORE:
                break;
            case ARRIVE:


                alarm.setArrived();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, ParseTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ARRIVED);

                break;
            case ABORT:

                alarm.setAborted();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, ParseTask.EVENT_TYPE.ABORT)
                        .event(ctx.getString(R.string.event_aborted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ABORT);

                break;

            case FINISH:

                alarm.setFinished();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, ParseTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_FINISHED);

//                ToastHelper.toast(ctx, ctx.getString(R.string.alarm_finished_at_client, clientName));


                break;

            case OPEN_WRITE_REPORT:
                ParseTaskCreateReportActivity.start(ctx, alarm);
                break;

            default:
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return alarm;


        }


        alarm.pinThenSaveEventually();

        if (event != null) {
            event.saveAsync();
        }


        return alarm;

    }






}
