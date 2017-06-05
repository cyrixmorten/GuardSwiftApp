package com.guardswift.ui.parse.documentation.report.edit;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandler;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandlerActivity;
import com.guardswift.ui.view.card.EventLogCard;
import com.parse.ParseQueryAdapter;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by cyrix on 11/21/15.
 */
public class ReportEditRecycleAdapter extends ParseRecyclerQueryAdapter<EventLog, ReportEditRecycleAdapter.ReportViewHolder> {


    private static final String TAG = ReportEditRecycleAdapter.class.getSimpleName();

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        public EventLogCard eventLogCard;

        public ReportViewHolder(EventLogCard eventLogCard) {
            super(eventLogCard);

            this.eventLogCard = eventLogCard;
        }

    }

    private WeakReference<FragmentActivity> activityWeakReference;

    public ReportEditRecycleAdapter(FragmentActivity activity, ParseQueryAdapter.QueryFactory<EventLog> queryFactory) {
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
    public void onBindViewHolder(final ReportViewHolder holder, int position) {
        final EventLog eventLog = getItem(position);
        Log.d(TAG, "onBindViewHolder: " + eventLog);
        final FragmentActivity activity = activityWeakReference.get();


        final boolean isExtraTimeEvent = eventLog.getEventCode() == EventLog.EventCodes.CIRCUITUNIT_EXTRA_TIME;
        final boolean isStaticReport = getItemViewType(position) == GSTask.TASK_TYPE.STATIC.ordinal();

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


        eventLogCard.onEventClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_TYPE);
            }
        });


        eventLogCard.onAmountClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new CommonDialogsBuilder.BetterPicks(activity.getSupportFragmentManager())
                        .enterEventAmount(eventLog.getEvent(),
                                new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
                                    @Override
                                    public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
                                        // update locally
                                        eventLog.setAmount(number.intValue());
                                        // update card
                                        eventLogCard.setEventLog(eventLog);
                                        // save online
                                        eventLog.pinThenSaveEventually();
                                    }
                                })
                        .show();
            }
        });

        eventLogCard.onPeopleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_PEOPLE);
            }
        });

        eventLogCard.onLocationsClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_LOCATIONS);
            }
        });

        eventLogCard.onDeleteClickListener(new View.OnClickListener()

                                           {
                                               @Override
                                               public void onClick(View view) {
                                                   new CommonDialogsBuilder.MaterialDialogs(activity).okCancel(R.string.delete, activity.getString(R.string.confirm_delete, eventLog.getSummaryString()), new MaterialDialog.SingleButtonCallback() {
                                                       @Override
                                                       public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {

                                                           notifyItemRemoved(getItems().indexOf(eventLog));
                                                           getItems().remove(eventLog);

                                                           eventLog.deleteEventually();
                                                       }
                                                   }).show();
                                               }
                                           }

        );

        eventLogCard.onTimestampClickListener(new View.OnClickListener()

                                              {
                                                  @Override
                                                  public void onClick(View view) {
                                                      final DateTime timestamp = new DateTime(eventLog.getDeviceTimestamp());

                                                      RadialTimePickerDialogFragment timePickerDialog = new RadialTimePickerDialogFragment()
                                                              .setStartTime(timestamp.getHourOfDay(), timestamp.getMinuteOfHour())
                                                              .setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                                                                  @Override
                                                                  public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                                                                      final Calendar cal = Calendar.getInstance();
                                                                      cal.setTime(timestamp.toDate());
                                                                      cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                                      cal.set(Calendar.MINUTE, minute);
                                                                      // update locally
                                                                      eventLog.setDeviceTimestamp(cal.getTime());
                                                                      // update card
                                                                      eventLogCard.setEventLog(eventLog);
                                                                      // update online
                                                                      eventLog.pinThenSaveEventually();
                                                                  }
                                                              })
                                                              .setThemeDark()
                                                              .setForced24hFormat();
                                                      timePickerDialog.show(activity.getSupportFragmentManager(), "FRAG_TAG_TIME_PICKER");
                                                  }
                                              }

        );

        eventLogCard.onRemarksClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    UpdateEventHandlerActivity.newInstance(activity, eventLog, UpdateEventHandler.REQUEST_EVENT_REMARKS);
                                                }
                                            }
        );
    }
}
