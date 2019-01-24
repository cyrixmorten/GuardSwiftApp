package com.guardswift.ui.parse.documentation.report.edit;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandler;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandlerActivity;
import com.guardswift.ui.view.card.EventLogCard;
import com.parse.ParseQueryAdapter;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by cyrix on 11/21/15.
 */
public class ReportEditRecycleAdapter extends ParseRecyclerQueryAdapter<EventLog, ReportEditRecycleAdapter.ReportViewHolder> {


    private static final String TAG = ReportEditRecycleAdapter.class.getSimpleName();

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        EventLogCard eventLogCard;

        ReportViewHolder(EventLogCard eventLogCard) {
            super(eventLogCard);

            this.eventLogCard = eventLogCard;
        }

    }

    private WeakReference<FragmentActivity> activityWeakReference;

    ReportEditRecycleAdapter(FragmentActivity activity, ParseQueryAdapter.QueryFactory<EventLog> queryFactory) {
        super(queryFactory);
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (activityWeakReference.get() != null) {
            EventLogCard eventLogCard = new EventLogCard(activityWeakReference.get());

            return new ReportViewHolder(eventLogCard);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        EventLog eventLog = getItem(position);
        return eventLog.getTaskType().ordinal();
    }

    @Override
    public void onBindViewHolder(final ReportViewHolder holder, final int position) {
        final EventLog eventLog = getItem(position);
        Log.d(TAG, "onBindViewHolder: " + eventLog);
        final FragmentActivity activity = activityWeakReference.get();


        final boolean isExtraTimeEvent = eventLog.getEventCode() == EventLog.EventCodes.REGULAR_EXTRA_TIME;
        final boolean isStaticReport = getItemViewType(position) == ParseTask.TASK_TYPE.STATIC.ordinal();

        final EventLogCard eventLogCard = holder.eventLogCard;

        eventLogCard.setEventLog(eventLog);

        // STATIC REPORTS ONLY HAS REMARKS
        if (isStaticReport) {
            eventLogCard.setCopyToReportEnabled(false);
            eventLogCard.setDeletable(true);
            eventLogCard.setEditable(false);
            eventLogCard.setTimestamped(true);
            eventLogCard.setHeaderVisibility(VISIBLE);
            eventLogCard.setEventVisibility(GONE);
            eventLogCard.setAmountVisibility(GONE);
            eventLogCard.setPeopleVisibility(GONE);
            eventLogCard.setLocationsVisibility(GONE);
            eventLogCard.setRemarksVisibility(VISIBLE);
        } else {
            eventLogCard.setCopyToReportEnabled(false);
            eventLogCard.setDeletable(true);
            eventLogCard.setEditable(true);
            eventLogCard.setTimestamped(true);
            eventLogCard.setHeaderVisibility(VISIBLE);
            eventLogCard.setEventVisibility(VISIBLE);
            eventLogCard.setAmountVisibility(VISIBLE);
            eventLogCard.setPeopleVisibility(isExtraTimeEvent ? GONE : VISIBLE);
            eventLogCard.setLocationsVisibility(isExtraTimeEvent ? GONE : VISIBLE);
            eventLogCard.setRemarksVisibility(VISIBLE);
        }


        eventLogCard.onEventClickListener(view -> UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_TYPE));


        eventLogCard.onAmountClickListener(view -> new CommonDialogsBuilder.BetterPicks(activity.getSupportFragmentManager())
                .enterEventAmount(eventLog.getEvent(),
                        (reference, number, decimal, isNegative, fullNumber) -> {
                            // update locally
                            eventLog.setAmount(number.intValue());
                            // update card
                            eventLogCard.setEventLog(eventLog);
                            // save online
                            eventLog.saveEventuallyAndNotify();
                        })
                .show());

        eventLogCard.onPeopleClickListener(view -> UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_PEOPLE));

        eventLogCard.onLocationsClickListener(view -> UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_LOCATIONS));

        eventLogCard.onDeleteClickListener(view -> new CommonDialogsBuilder.MaterialDialogs(activity).okCancel(R.string.delete, activity.getString(R.string.confirm_delete, eventLog.getSummaryString()), (materialDialog, dialogAction) -> {

                    int position1 = holder.getAdapterPosition();
                    getItems().remove(position1);
                    if (position1 > 0) {
                        notifyItemRemoved(position1);
                    } else {
                        notifyDataSetChanged();
                    }

                    if (eventLog.isArrivalEvent() && eventLog.getTask() != null) {
                        ParseTask task = eventLog.getTask();
                        task.deleteArrival();
                        task.saveEventuallyAndNotify();
                    }

                    eventLog.deleteEventually();
                }).show()

        );

        eventLogCard.onTimestampClickListener(view -> {
            final DateTime timestamp = new DateTime(eventLog.getDeviceTimestamp());

            RadialTimePickerDialogFragment timePickerDialog = new RadialTimePickerDialogFragment()
                    .setStartTime(timestamp.getHourOfDay(), timestamp.getMinuteOfHour())
                    .setOnTimeSetListener((dialog, hourOfDay, minute) -> {
                        final Calendar cal = Calendar.getInstance();
                        cal.setTime(timestamp.toDate());
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute);
                        // update locally
                        eventLog.setDeviceTimestamp(cal.getTime());
                        // update card
                        eventLogCard.setEventLog(eventLog);
                        // update online
                        eventLog.saveEventuallyAndNotify();
                    })
                    .setThemeDark()
                    .setForced24hFormat();
            timePickerDialog.show(activity.getSupportFragmentManager(), "FRAG_TAG_TIME_PICKER");
        }

        );

        eventLogCard.onRemarksClickListener(view -> UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_REMARKS)
        );
    }
}
