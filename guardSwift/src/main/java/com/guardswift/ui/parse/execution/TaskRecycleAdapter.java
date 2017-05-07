package com.guardswift.ui.parse.execution;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapSize;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PositionedViewHolder;
import com.guardswift.ui.parse.data.checkpoint.CheckpointActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditActivity;
import com.guardswift.ui.parse.documentation.report.view.ReportHistoryListFragment;
import com.guardswift.ui.parse.execution.circuit.TaskDescriptionActivity;
import com.guardswift.util.AnimationHelper;
import com.guardswift.util.OpenLocalPDF;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQueryAdapter;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import static com.guardswift.core.tasks.controller.TaskController.ACTION;

public class TaskRecycleAdapter<T extends BaseTask> extends ParseRecyclerQueryAdapter<T, TaskRecycleAdapter.TaskViewHolder> {

    private static final String TAG = TaskRecycleAdapter.class.getSimpleName();


    public interface TaskActionCallback<T extends BaseTask> {
        void onActionOpen(Context context, T task);

        void onActionAccept(Context context, T task);

        void onActionArrive(Context context, T task);

        void onActionAbort(Context context, T task);

        void onActionFinish(Context context, T task);
    }

    public static class StaticTaskViewHolder extends TaskViewHolder<StaticTask> {

        @BindView(R.id.timeStart)
        TextView vTimeStart;
        @BindView(R.id.timeEnd)
        TextView vTimeEnd;
        @BindView(R.id.aTvClock)
        AwesomeTextView iconClock;

        public StaticTaskViewHolder(View v) {
            super(v, null);

            vTimeEnd.setVisibility(View.GONE);
        }

        @Override
        public void onActionOpen(final Context context, final StaticTask task) {
            super.onActionOpen(context, task);

            if (task.isPending()) {
                new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.static_guarding_report, context.getString(R.string.begin_static_guarding_at_client, task.getClientName()), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        // show indeterminate progress
                        final MaterialDialog loadingDialog = new CommonDialogsBuilder.MaterialDialogs(context).indeterminate(R.string.report_creating).show();

                        Guard guard = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().getLoggedIn();
                        task.setStartedBy(guard);

                        task.saveInBackground().continueWith(new Continuation<Void, Object>() {
                            @Override
                            public Object then(Task<Void> taskResult) throws Exception {
                                Exception error = taskResult.getError();
                                if (error != null) {
                                    loadingDialog.cancel();
                                    new CommonDialogsBuilder.MaterialDialogs(context).missingInternetContent().show();

                                    new HandleException(context, TAG, "Failed to create static task", error);
                                    return null;
                                }

                                task.addReportEntry(context, context.getString(R.string.started), null, new GetCallback<EventLog>() {
                                    @Override
                                    public void done(EventLog object, ParseException e) {
                                        ReportEditActivity.start(context, task);

                                        loadingDialog.cancel();
                                    }
                                });

                                return null;

                            }
                        });
                    }
                }).show();
            } else {
                ReportEditActivity.start(context, task);
            }

        }

        @Override
        protected void setupTaskActionButtons(final Context context, final StaticTask task) {
            this.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActionOpen(context, task);
                }
            });
        }

        @Override
        public void update(Context context, GSTask task) {
            super.update(context, task);

            if (task instanceof StaticTask) {
                vContentFooter.setVisibility(View.GONE);
                vContentHeader.setVisibility(View.GONE);
                Date timeStarted = ((StaticTask) task).getTimeArrived();
                if (timeStarted != null) {
                    vTimeStart.setText(DateFormat.getLongDateFormat(context).format(timeStarted));
                    vTimeEnd.setVisibility(View.GONE);
                    iconClock.setVisibility(View.VISIBLE);
                } else {
                    vTimeStart.setVisibility(View.GONE);
                    vTimeEnd.setVisibility(View.GONE);
                    iconClock.setVisibility(View.GONE);
                }

                setupTaskActionButtons(context, (StaticTask) task);
            }
            super.update(context, task);
        }
    }

    public static class AlarmTaskViewHolder extends TaskViewHolder<ParseTask> {

        @BindView(R.id.tv_central)
        TextView vCentral;
        @BindView(R.id.tv_date)
        TextView vDate;


        public AlarmTaskViewHolder(View v, RemoveItemCallback removeItemCallback) {
            super(v, removeItemCallback);
        }


        @Override
        public void onActionOpen(Context context, ParseTask task) {
            super.onActionOpen(context, task);

            boolean inverseSelection = vContentFooter.getVisibility() == View.GONE;

            // Save the selected positions to the SparseBooleanArray
            if (task.isArrived() || inverseSelection) {
                expandFooter();
            } else {
                collapseFooter();
            }

        }

        public void abort(final Context context, final ParseTask task) {
            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_aborted, task.getClientName()), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    AlarmTaskViewHolder.super.onActionAbort(context, task);
                }
            }).show();
        }

        @Override
        public void onActionFinish(final Context context, final ParseTask task) {

            if (task.isAccepted()) {
                abort(context, task);
                return;
            }

            // arrived
            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_finished), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            AlarmTaskViewHolder.super.onActionFinish(context, task);
                        }
                    }
            ).show();

        }


        @Override
        protected void setupTaskActionButtons(final Context context, final ParseTask task) {
            super.setupTaskActionButtons(context, task);

            vBtnViewReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReportEditActivity.start(context, task);
                }
            });

            vBtnAddNewEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CreateEventHandlerActivity.start(context, task);
                }
            });

            vBtnReportHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = ReportHistoryListFragment.newInstance(task.getClient(), GSTask.TASK_TYPE.ALARM);
                    GenericToolbarActivity.start(
                            context,
                            context.getString(R.string.reports),
                            task.getClient().getFullAddress(),
                            fragment);
                }
            });

            vBtnTaskdescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    TaskDescriptionActivity.start(context, task);
                }
            });


            vBtnClientContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialogsBuilder.MaterialDialogs(context).clientContacts(task.getClient()).show();
                }
            });
        }

        @Override
        public void update(final Context context, GSTask task) {
            super.update(context, task);

            if (task instanceof ParseTask && task.getTaskType() == GSTask.TASK_TYPE.ALARM) {
                final ParseTask alarmTask = (ParseTask) task;

//                vBtnAborted.setVisibility(View.GONE);
                vBtnTaskdescription.setVisibility(View.GONE);

                if (task.isPending()) {
                    vBtnAccepted.setVisibility(View.VISIBLE);
                    vBtnArrived.setVisibility(View.GONE);
//                    vBtnAborted.setVisibility(View.GONE);
                    vBtnFinished.setVisibility(View.GONE);
                }

                if (task.isAccepted()) {
                    vBtnFinished.setText(context.getString(R.string.onActionAbort));
                }

                if (task.isAborted() || task.isFinished()) {
                    vBtnArrived.setVisibility(View.GONE);
                }

                String description = context.getText(R.string.security_level) + ": " + alarmTask.getPriority() + "\n" +
                        context.getText(R.string.keybox) + ": " + alarmTask.getKeybox() + "\n" +
                        context.getText(R.string.remarks) + ": " + alarmTask.getRemarks();

//                if (alarmTask.getGuard() != null) {
//                    description += "\n\n";
//                    description += context.getString(R.string.guard) + ": " + alarmTask.getGuard().getName();
//                }

                vTaskDesc.setText(description);
//                alarmTaskViewHolder.vTimeStart.setText(alarmTask.getTimeStartString());
//                alarmTaskViewHolder.vTimeEnd.setText(alarmTask.getTimeEndString());

                String centralName = alarmTask.getCentralName();


                vCentral.setText(centralName);
                vDate.setText(
                        new SpannableStringBuilder()
                                .append(DateFormat.getDateFormat(context).format(alarmTask.getCreatedAt()))
                                .append(" ")
                                .append(DateFormat.getTimeFormat(context).format(alarmTask.getCreatedAt()))
                );

                setupTaskActionButtons(context, alarmTask);


                if (alarmTask.getCentralName().equals("G4S")) {
                    View centralButton = vContentFooter.findViewById(R.id.button_central_pdf);
                    if (centralButton == null) {
                        Button g4spdf = new Button(new ContextThemeWrapper(context, android.R.style.Widget_DeviceDefault_Button_Borderless), null, android.R.style.Widget_DeviceDefault_Button_Borderless);
                        g4spdf.setId(R.id.button_central_pdf);
                        g4spdf.setText(context.getString(R.string.alarm_panels));
                        g4spdf.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new OpenLocalPDF(context, "G4S").execute();
                            }
                        });
                        vContentFooter.addView(g4spdf);
                    }
                }

                View centralButton = vContentFooter.findViewById(R.id.button_original_alarm_text);
                if (centralButton == null) {
                    Button originalAlarm = new Button(new ContextThemeWrapper(context, android.R.style.Widget_DeviceDefault_Button_Borderless), null, android.R.style.Widget_DeviceDefault_Button_Borderless);
                    originalAlarm.setId(R.id.button_original_alarm_text);
                    originalAlarm.setText(context.getString(R.string.alarm_original_text));
                    originalAlarm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.alarm, alarmTask.getOriginal()).show();
                        }
                    });
                    vContentFooter.addView(originalAlarm);
                }

            }
        }


    }

    public static class RegularTaskViewHolder extends TaskViewHolder<CircuitUnit> {

        @BindView(R.id.info)
        TextView vInfo;
        @BindView(R.id.layout_info)
        LinearLayout vInfoLayout;
        @BindView(R.id.timeStart)
        TextView vTimeStart;
        @BindView(R.id.timeEnd)
        TextView vTimeEnd;


        private FragmentManager fragmentManager;

        public RegularTaskViewHolder(View v, RemoveItemCallback removeItemCallback, FragmentManager fragmentManager) {
            super(v, removeItemCallback);

            this.fragmentManager = fragmentManager;
        }


        @Override
        public void onActionOpen(Context context, CircuitUnit task) {
            super.onActionOpen(context, task);

//            boolean inverseSelection = !selectedItems.get(getAdapterPosition(), false);
            boolean inverseSelection = vContentFooter.getVisibility() == View.GONE;

            // Save the selected positions to the SparseBooleanArray
            if (task.isArrived() || inverseSelection) {
                expandFooter();
            } else {
                collapseFooter();
            }

        }

        @Override
        public void onActionArrive(final Context context, final CircuitUnit task) {
            if (task.isWithinScheduledTime()) {
                super.onActionArrive(context, task);
            } else {
                // Show dialog explaining that time is outside scheduled
                new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.outside_schedule, R.string.arrived_outside_schedule, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        addArrivalEvent(context, task, new GetCallback<CircuitUnit>() {
                            @Override
                            public void done(CircuitUnit task, ParseException e) {
                                update(context, task);
                            }
                        });
                    }
                }).show();
            }
        }

        @Override
        public void onActionAbort(Context context, CircuitUnit task) {
            super.onActionAbort(context, task);
            collapseFooter();
        }

        @Override
        public void onActionFinish(final Context context, final CircuitUnit task) {
            confirmIfMissingSupervisions(context, task, new GetCallback<CircuitUnit>() {
                @Override
                public void done(CircuitUnit object, ParseException e) {
                    // Either not missing supervisions, or finish despite
                    finishWithChecks(context, task);
                }
            });
        }

        private void finishWithChecks(final Context context, final CircuitUnit task) {
            if (task.getTimesArrived() == 0 && fragmentManager != null) {
                missingArrivalTimestampDialog(context, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        addArrivalEvent(context, task, new GetCallback<CircuitUnit>() {
                            @Override
                            public void done(CircuitUnit object, ParseException e) {
                                RegularTaskViewHolder.super.onActionFinish(context, task);
                            }
                        });
                    }
                });
            } else {
                Log.e(TAG, "Should show missing arrival dialog but had no fragment manager");
                RegularTaskViewHolder.super.onActionFinish(context, task);
            }
        }

        private void confirmIfMissingSupervisions(final Context context, final CircuitUnit task, final GetCallback<CircuitUnit> okCallback) {
            int plannedSupervision = task.getPlannedSuperVisions();
            int timesArrived = task.getTimesArrived();
            int diff = plannedSupervision - timesArrived;

            if (plannedSupervision > 1 && diff != 0) {
                new CommonDialogsBuilder.MaterialDialogs(context).okCancel(
                        R.string.missing_supervisions,
                        context.getString(R.string.confirm_finish_missing_supervisions, diff),
                        new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                okCallback.done(task, null);
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();
            } else {
                okCallback.done(task, null);
            }
        }



        private void addArrivalEvent(final Context context, final CircuitUnit task, final GetCallback<CircuitUnit> callback) {
            final DateTime timestamp = new DateTime();
            RadialTimePickerDialogFragment timePickerDialog = RadialTimePickerDialogFragment
                    .newInstance(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                                     @Override
                                     public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                                         final Calendar cal = Calendar.getInstance();
                                         cal.setTime(new Date());
                                         cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                         cal.set(Calendar.MINUTE, minute);


                                         new EventLog.Builder(context)
                                                 .taskPointer(task, GSTask.EVENT_TYPE.ARRIVE)
                                                 .event(context.getString(R.string.event_arrived))
                                                 .automatic(false)
                                                 .deviceTimeStamp(cal.getTime())
                                                 .eventCode(EventLog.EventCodes.CIRCUITUNIT_ARRIVED).saveAsync();


                                         callback.done(task, null);

                                     }
                                 }, timestamp.getHourOfDay(), timestamp.getMinuteOfHour(),
                            DateFormat.is24HourFormat(context));
            timePickerDialog.show(fragmentManager, "FRAG_TAG_ARRIVAL_TIME_PICKER");
        }

        private void missingArrivalTimestampDialog(final Context context, MaterialDialog.SingleButtonCallback callback) {
            new CommonDialogsBuilder.MaterialDialogs(context).ok(
                    R.string.missing_arrival_time,
                    R.string.please_enter_arrival_time,
                    callback)
                    .canceledOnTouchOutside(false)
                    .show();
        }

        private void extraTimeDialog(final Context context, final CircuitUnit task) {
            new MaterialDialog.Builder(context)
                    .title(R.string.extra_time_spend)
                    .items(R.array.extra_time_minutes_strings)
                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int index, CharSequence value) {
                            String[] extra_time_values = context.getResources()
                                    .getStringArray(
                                            R.array.extra_time_minutes_values);
                            int minutes = Integer
                                    .parseInt(extra_time_values[index]);
                            if (minutes != 0) {
                                new EventLog.Builder(context)
                                        .taskPointer(task, GSTask.EVENT_TYPE.OTHER)
                                        .event(context.getString(R.string.event_extra_time))
                                        .amount(minutes)
                                        .remarks(value.toString())
                                        .eventCode(EventLog.EventCodes.CIRCUITUNIT_EXTRA_TIME).saveAsync();
                            }


                            return true;
                        }
                    })
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .show();
        }

        @Override
        protected void setupTaskActionButtons(final Context context, final CircuitUnit task) {
            super.setupTaskActionButtons(context, task);

            vBtnViewReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReportEditActivity.start(context, task);
                }
            });

            vBtnAddNewEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CreateEventHandlerActivity.start(context, task);
                }
            });

            vBtnReportHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = ReportHistoryListFragment.newInstance(task.getClient(), GSTask.TASK_TYPE.REGULAR);
                    GenericToolbarActivity.start(
                            context,
                            context.getString(R.string.reports),
                            task.getClient().getFullAddress(),
                            fragment);
                }
            });

            vBtnTaskdescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TaskDescriptionActivity.start(context, task);
                }
            });

            vBtnCheckpoints.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckpointActivity.start(context, task);
                }
            });

            vBtnClientContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialogsBuilder.MaterialDialogs(context).clientContacts(task.getClient()).show();
                }
            });
        }

        public void update(final Context context, final GSTask task) {
            super.update(context, task);

            if (task instanceof CircuitUnit) {
                CircuitUnit circuitUnit = (CircuitUnit) task;
                vBtnTaskdescription.setVisibility((!circuitUnit.getDescription().isEmpty()) ? View.VISIBLE : View.GONE);

                vTaskDesc.setText(circuitUnit.getName());
                vTimeStart.setText(circuitUnit.getTimeStartString());
                vTimeEnd.setText(circuitUnit.getTimeEndString());


                if (vInfoLayout.getChildCount() > 1) {
                    vInfoLayout.removeViewAt(0);
                }

                ImageView iconView = new ImageView(context);
                GoogleMaterial.Icon icon = (circuitUnit.isRaid()) ? GoogleMaterial.Icon.gmd_car : GoogleMaterial.Icon.gmd_walk;
                iconView.setImageDrawable(new IconicsDrawable(context)
                        .icon(icon)
                        .color(Color.DKGRAY)
                        .sizeDp(24));

                iconView.setPadding(0, 0, 12, 0);

                vInfoLayout.addView(iconView, 0);

                String infoText = context.getString(R.string.times_supervised,
                        circuitUnit.getTimesArrived(),
                        circuitUnit.getPlannedSuperVisions());

                vInfo.setText(infoText);

                setupTaskActionButtons(context, circuitUnit);

                if (circuitUnit.completeButNotFinished()) {
                    updateTaskState(context, GSTask.TASK_STATE.ACCEPTED);
                }
            }

            // Add extra time button setup
            if (task instanceof CircuitUnit) {
                final CircuitUnit circuitUnit = (CircuitUnit) task;
                vBtnAddExtraTime.setVisibility(View.VISIBLE);
                vBtnAddExtraTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        extraTimeDialog(context, circuitUnit);
                    }
                });
            } else {
                vBtnAddExtraTime.setVisibility(View.GONE);
            }
        }
    }


    public static class DistrictWatchTaskViewHolder extends TaskViewHolder<DistrictWatchClient> {

        @BindView(R.id.timesVisited_actual)
        TextView vTimesVisited_actual;
        @BindView(R.id.timesVisited_expected)
        TextView vTimesVisited_expected;

        public DistrictWatchTaskViewHolder(View itemView, RemoveItemCallback removeItemCallback) {
            super(itemView, removeItemCallback);

//            this.vBtnAborted.setVisibility(View.GONE);
            this.vBtnFinished.setVisibility(View.GONE);
            this.vClientNumber.setVisibility(View.GONE);
            this.vTaskDesc.setVisibility(View.GONE);

        }


        @Override
        public void onActionArrive(final Context context, final DistrictWatchClient task) {
            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_arrived), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {

                    int newTimesArrived = task.getTimesArrived() + 1;
                    vTimesVisited_actual.setText(String.valueOf(newTimesArrived));

                    if (newTimesArrived == task.getSupervisions()) {
                        removeItemCallback.removeAt(getAdapterPosition());
                    }

                    performTaskAction(context, task, ACTION.ARRIVE);
                }
            }).show();


        }

        @Override
        public void update(Context context, GSTask task) {
            if (task instanceof DistrictWatchClient) {
                vContentFooter.setVisibility(View.GONE);
                DistrictWatchClient districtWatchClient = (DistrictWatchClient) task;
                // swap name and address to enhance the address
                vName.setText(districtWatchClient.getFullAddress());
                vAddress.setText(districtWatchClient.getClientName());
                vTimesVisited_actual.setText(String.valueOf(districtWatchClient.getTimesArrived()));
                vTimesVisited_expected.setText(String.valueOf(districtWatchClient.getSupervisions()));
                if (districtWatchClient.getTimesArrived() == districtWatchClient.getSupervisions()) {
                    vBtnArrived.setEnabled(false);
                }

                setupTaskActionButtons(context, (DistrictWatchClient) task);
            }
            super.update(context, task);
        }
    }


    static class TaskViewHolder<T extends BaseTask> extends PositionedViewHolder implements TaskActionCallback<T> {


        @BindView(R.id.cardview)
        CardView cardview;

        @BindViews({R.id.content_colorBorder_top, R.id.content_colorBorder_bottom})
        List<ImageView> vColorBorders;

        @BindView(R.id.task_state_arrived)
        BootstrapButton vBtnArrived;
        @BindView(R.id.task_state_accepted)
        BootstrapButton vBtnAccepted;
        //        @BindView(R.id.task_state_aborted)
//        BootstrapButton vBtnAborted;
        @BindView(R.id.task_state_finished)
        BootstrapButton vBtnFinished;

        @BindView(R.id.content_header)
        LinearLayout vContentHeader;
        @BindView(R.id.content_body)
        LinearLayout vContentBody;
        @BindView(R.id.content_footer)
        LinearLayout vContentFooter;

        @BindView(R.id.clientNumber)
        TextView vClientNumber;
        @BindView(R.id.clientName)
        TextView vName;
        @BindView(R.id.clientAddress)
        TextView vAddress;
        @BindView(R.id.taskTypeDesc)
        TextView vTaskDesc;
        @BindView(R.id.tv_guard_name)
        TextView tvGuardName;

        // footer buttons
        @BindView(R.id.btn_view_report)
        Button vBtnViewReport;
        @BindView(R.id.btn_new_event)
        Button vBtnAddNewEvent;
        @BindView(R.id.btn_extra_time)
        Button vBtnAddExtraTime;
        @BindView(R.id.btn_report_history)
        Button vBtnReportHistory;
        @BindView(R.id.btn_task_description)
        Button vBtnTaskdescription;
        @BindView(R.id.btn_client_info)
        Button vBtnClientContacts;
        @BindView(R.id.btn_checkpoints)
        Button vBtnCheckpoints;


        protected RemoveItemCallback removeItemCallback;

        public void update(Context context, GSTask task) {
            tvGuardName.setText((task.getGuard() != null) ? task.getGuard().getName() : "");
            setTaskState(context, task);
        }

        @Override
        public void onActionOpen(Context context, T task) {

        }

        protected void expandFooter() {
            AnimationHelper.expand(vContentFooter);
            selectedItems.put(getAdapterPosition(), true);
        }

        protected void collapseFooter() {
            AnimationHelper.collapse(vContentFooter);
            selectedItems.delete(getAdapterPosition());
        }

        @Override
        public void onActionAccept(final Context context, final T task) {

            performTaskAction(context, task, ACTION.ACCEPT);


            vBtnAccepted.setVisibility(View.GONE);
            vBtnArrived.setVisibility(View.VISIBLE);

//            if (!task.getTaskType().equals(GSTask.TASK_TYPE.ALARM)) {
//                vBtnAborted.setVisibility(View.VISIBLE);
//            }

            vBtnFinished.setVisibility(View.VISIBLE);

        }

        @Override
        public void onActionArrive(final Context context, final T task) {

            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_arrived), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    performTaskAction(context, task, ACTION.ARRIVE).onSuccess(new Continuation<GSTask, Object>() {
                        @Override
                        public Object then(Task<GSTask> task) throws Exception {
                            update(context, task.getResult());
                            return null;
                        }
                    });
                }
            }).show();
        }

        @Override
        public void onActionAbort(final Context context, final T task) {
            performTaskAction(context, task, ACTION.ABORT);
        }

        @Override
        public void onActionFinish(Context context, T task) {
            performTaskAction(context, task, ACTION.FINISH);
        }


        public TaskViewHolder(View v, RemoveItemCallback removeItemCallback) {
            super(v);
            ButterKnife.bind(this, v);

            this.vBtnAccepted.setBootstrapSize(DefaultBootstrapSize.LG);
            this.vBtnArrived.setBootstrapSize(DefaultBootstrapSize.LG);
//            this.vBtnAborted.setBootstrapSize(DefaultBootstrapSize.LG);
            this.vBtnFinished.setBootstrapSize(DefaultBootstrapSize.LG);

            setRemoveItemCallback(removeItemCallback);

        }

        protected void setupTaskActionButtons(final Context context, final T task) {


            this.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActionOpen(context, task);
                }
            });

            this.vBtnAccepted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActionAccept(context, task);
                }
            });

            this.vBtnArrived.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActionArrive(context, task);
                }
            });

//            this.vBtnAborted.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    onActionAbort(context, task);
//                }
//            });


            this.vBtnFinished.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onActionFinish(context, task);
                }
            });

        }


        public void setRemoveItemCallback(RemoveItemCallback removeItemCallback) {
            this.removeItemCallback = removeItemCallback;
        }

        @SuppressWarnings("unchecked")
        protected Task<GSTask> performTaskAction(final Context context, final GSTask task, final ACTION action) {

            final TaskCompletionSource result = new TaskCompletionSource<>();

            final GSTask.TASK_STATE previousTaskState = task.getTaskState();
            final TaskController taskController = task.getController();
            if (taskController.canPerformAction(action, task)) {
                new AsyncTask<Void, Void, GSTask>() {

                    @Override
                    protected void onPreExecute() {
                        GSTask.TASK_STATE toState = taskController.translatesToState(action);
                        updateTaskState(context, previousTaskState, toState);
                        super.onPreExecute();
                    }

                    @Override
                    protected GSTask doInBackground(Void... voids) {
                        // might contain LDS lookup
                        return taskController.performAction(action, task);
                    }

                    @Override
                    protected void onPostExecute(GSTask updatedTask) {
                        super.onPostExecute(updatedTask);
                        result.setResult(updatedTask);
                    }
                }.execute();
            } else {
                result.setResult(task);
            }

            return result.getTask();
        }


        public void setTaskState(Context context, GSTask task) {
            GSTask.TASK_STATE state = task.getTaskState();
            updateTaskState(context, state);
        }

        public void updateTaskState(Context context, GSTask.TASK_STATE toState) {
            int color = getTaskStateColor(context, toState);
            updateTaskStateButtons(context, toState);
            for (View border : this.vColorBorders) {
                tintBackgroundColor(border, color, 100);
            }
            tintBackgroundColor(this.cardview, color, 50);
        }

        public void removeTask() {
            if (removeItemCallback != null) {
                removeItemCallback.removeAt(getAdapterPosition());
            }
        }

        public void updateTaskState(Context context, GSTask.TASK_STATE fromState, GSTask.TASK_STATE toState) {

            boolean fromActiveState = fromState == GSTask.TASK_STATE.PENDING || fromState == GSTask.TASK_STATE.ACCEPTED || fromState == GSTask.TASK_STATE.ARRIVED;
            boolean toFinishedState = toState == GSTask.TASK_STATE.FINISHED || toState == GSTask.TASK_STATE.ABORTED;

            boolean fromFinishedState = fromState == GSTask.TASK_STATE.FINISHED || fromState == GSTask.TASK_STATE.ABORTED;
            boolean toActiveState = toState == GSTask.TASK_STATE.PENDING || toState == GSTask.TASK_STATE.ACCEPTED || toState == GSTask.TASK_STATE.ARRIVED;

            if (fromActiveState && toFinishedState) {
                removeTask();
                return;
            }
            if (fromFinishedState && toActiveState) {
                removeTask();
                return;
            }
            updateTaskState(context, fromState, toState, false, true);
        }

        public void updateTaskState(Context context, GSTask.TASK_STATE fromState, GSTask.TASK_STATE toState, boolean isNew, boolean animate) {
            updateTaskStateColor(context, fromState, toState, isNew, animate);
            updateTaskStateButtons(context, toState);
        }

        private void updateTaskStateButtons(Context context, GSTask.TASK_STATE state) {
            bootstrapActionButtonDefaults(context, this.vBtnArrived);
//            bootstrapActionButtonDefaults(context, this.vBtnAborted);
            bootstrapActionButtonDefaults(context, this.vBtnFinished);

            int colorRes = getTaskStateColorResource(state);
            switch (state) {
                case ARRIVED:
                    bootstrapActionButtonSelect(context, this.vBtnArrived, colorRes);
                    break;
//                case ABORTED:
//                    bootstrapActionButtonSelect(context, this.vBtnAborted, colorRes);
//                    break;
                case FINISHED:
                    bootstrapActionButtonSelect(context, this.vBtnFinished, colorRes);
//                    bootstrapActionDisableAll();
                    break;
            }
        }

        private void bootstrapActionButtonDefaults(Context context, BootstrapButton button) {
            button.setSelected(false);
            button.setEnabled(true);
            button.setBackgroundResource(R.color.bootstrap_gray);
            button.setTextColor(context.getResources().getColor(R.color.bootstrap_gray_light));
        }

        private void bootstrapActionButtonSelect(Context context, BootstrapButton button, int colorRes) {
            button.setSelected(true);
            button.setEnabled(false);
            button.setTextColor(context.getResources().getColor(R.color.bootstrap_gray_lighter));
            button.setBackgroundResource(colorRes);
        }

        private void bootstrapActionDisableAll() {
            this.vBtnArrived.setEnabled(false);
//            this.vBtnAborted.setEnabled(false);
            this.vBtnFinished.setEnabled(false);
        }

        private void updateTaskStateColor(Context context, GSTask.TASK_STATE fromState, GSTask.TASK_STATE toState, boolean isNew, boolean animate) {

            int prevStateColor = getTaskStateColor(context, fromState);
            int toColor = getTaskStateColor(context, toState);
            int tintDuration = (animate) ? 500 : 0;
            int fromColor = (isNew) ? Color.WHITE : prevStateColor;

//            Log.d(TAG, "updateTaskStateColor " + fromState + "->" + toState);
//            Log.d(TAG, "updateTaskStateColor " + fromColor + "->" + toColor);

            tintBackgroundColor(this.vColorBorders, fromColor, toColor, 100, tintDuration);
            tintBackgroundColor(this.cardview, fromColor, toColor, 50, tintDuration);

        }

        private int getTaskStateColor(Context context, GSTask.TASK_STATE state) {
            int colorRes = getTaskStateColorResource(state);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getColor(colorRes);
            } else {
                return context.getResources().getColor(colorRes);
            }
        }

        private int getTaskStateColorResource(GSTask.TASK_STATE state) {
            int color = R.color.bootstrap_brand_info; // default pending
            switch (state) {
                case PENDING:
                    color = R.color.bootstrap_brand_info;
                    break;
                case ACCEPTED:
                    color = R.color.bootstrap_brand_warning;
                    break;
                case ARRIVED:
                    color = R.color.bootstrap_brand_success;
                    break;
                case ABORTED:
                    color = R.color.bootstrap_brand_danger;
                    break;
                case FINISHED:
                    color = R.color.bootstrap_gray_light;
                    break;
            }
            return color;
        }

        private <V extends View> void tintBackgroundColor(List<V> views, int colorFrom, int colorTo, final int alpha, int duration) {
            for (View view : views) {
                tintBackgroundColor(view, colorFrom, colorTo, alpha, duration);
            }
        }

        private void tintBackgroundColor(final View view, int color, int alpha) {
            int toColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
            if (view instanceof CardView) {
                ((CardView) view).setCardBackgroundColor(toColor);
                return;
            }
            view.setBackgroundColor(toColor);
        }

        private void tintBackgroundColor(final View view, int colorFrom, int colorTo, final int alpha, int duration) {
            if (duration == 0) {
                tintBackgroundColor(view, colorTo, alpha);
                return;
            }

            final float[] from = new float[3],
                    to = new float[3];


            Color.colorToHSV(colorFrom, from);   // from white
            Color.colorToHSV(colorTo, to);     // to red

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
            anim.setDuration(duration);                              // for 300 ms

            final float[] hsv = new float[3];                  // transition color
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // Transition along each axis of HSV (hue, saturation, value)
                    hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                    hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                    hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                    int color = Color.HSVToColor(alpha, hsv);
                    if (view instanceof CardView) {
                        ((CardView) view).setCardBackgroundColor(color);
                    } else {
                        view.setBackgroundColor(color);
                    }
                }
            });

            anim.start();
        }

    }

    private int lastPosition = -1;
    private static SparseBooleanArray selectedItems = new SparseBooleanArray();

    public TaskRecycleAdapter(Context context, ParseQueryAdapter.QueryFactory<T> factory) {
        super(factory, false);
        this.context = context;
    }

    public TaskRecycleAdapter(Context context, FragmentManager fragmentManager, ParseQueryAdapter.QueryFactory<T> factory) {
        super(factory, false);
        this.context = context;
        this.fragmentManager = fragmentManager;
    }


    private boolean isNewlyDisplayed(int position) {
        if (position > lastPosition) {
            lastPosition = position;
            return true;
        }
        return false;
    }


    @Override
    public int getItemViewType(int position) {
        return getItem(position).getTaskType().ordinal();
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, final int position) {

        boolean isNew = isNewlyDisplayed(position);

        final GSTask task = getItem(position);

        Client client = task.getClient();
        if (client != null) {
            holder.vClientNumber.setText(client.getId());
            holder.vName.setText(client.getName());
            holder.vAddress.setText(client.getFullAddress());
            holder.vBtnCheckpoints.setVisibility((client.hasCheckPoints()) ? View.VISIBLE : View.GONE);
            holder.vBtnClientContacts.setVisibility((!client.getContactsWithNames().isEmpty()) ? View.VISIBLE : View.GONE);
        }

        holder.vContentFooter.setVisibility((task.isArrived() || selectedItems.get(position, false)) ? View.VISIBLE : View.GONE);


        holder.update(context, task);

        debugGeofenceStatus(task, holder);

        new PositionedViewHolder.CalcDistanceAsync(task, holder).execute();
//        new UpdateTaskStateAsync(task, holder, isNew).execute();
    }

    private void debugGeofenceStatus(GSTask task, TaskViewHolder holder) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        LinearLayout linearLayout = (LinearLayout) holder.vContentBody.findViewById(R.id.layout_debug_geofence);
        if (linearLayout == null) {
            linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setId(R.id.layout_debug_geofence);

            holder.vContentBody.addView(linearLayout);
        }

        linearLayout.removeAllViews();

        linearLayout.addView(isGeofenced(task));
        linearLayout.addView(isWithinGeofence(task));
        linearLayout.addView(isOutsideGeofence(task));
        linearLayout.addView(isWithinScheduledTime(task));
    }

    private TextView isWithinScheduledTime(GSTask task) {
        TextView tv = new TextView(context);
        tv.setText(" TIME ");
        boolean isWithinScheduled = false;
        if (task instanceof CircuitUnit) {
            isWithinScheduled = ((CircuitUnit) task).isWithinScheduledTime();
        }


        tv.setTextColor((isWithinScheduled) ? Color.GREEN : Color.RED);

        return tv;
    }

    private TextView isOutsideGeofence(GSTask task) {
        boolean outsideGeofence = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().isMovedOutsideGeofence(task);

        TextView tv = new TextView(context);
        tv.setText(" OUTSIDE ");
        tv.setTextColor((outsideGeofence) ? Color.GREEN : Color.RED);

        return tv;
    }

    private TextView isWithinGeofence(GSTask task) {
        boolean withinGeofence = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().isWithinGeofence(task);

        TextView tv = new TextView(context);
        tv.setText(" WITHIN ");
        tv.setTextColor((withinGeofence) ? Color.GREEN : Color.RED);

        return tv;
    }

    private TextView isGeofenced(GSTask task) {
        boolean isGeofenced = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().isGeofenced(task);

        TextView tv = new TextView(context);
        tv.setText(" GEOFENCED ");
        tv.setTextColor((isGeofenced) ? Color.GREEN : Color.RED);

        return tv;
    }

    /**
     * Here is the key method to apply the animation
     */
//    private void setAnimation(View viewToAnimate, int clientPosition) {
//        // If the bound view wasn't previously displayed on screen, it's animated
//        if (clientPosition > lastPosition) {
//            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//            viewToAnimate.startAnimation(animation);
//            lastPosition = clientPosition;
//        }
//    }

    public interface RemoveItemCallback {
        void removeAt(int position);
    }

    private RemoveItemCallback defaultRemoveItemCallback = new RemoveItemCallback() {
        @Override
        public void removeAt(int position) {
            // saw crash reports due to java.lang.ArrayIndexOutOfBoundsException by getAdapterPosition() returning -1
            if (position >= 0) {
                removeItemAt(position);
            }
        }
    };

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.gs_card_task, parent, false);

        selectedItems = new SparseBooleanArray();


        TaskViewHolder holder = new TaskViewHolder(itemView, defaultRemoveItemCallback);

        LinearLayout contentBody = ButterKnife.findById(itemView, R.id.content_body);

        if (viewType == GSTask.TASK_TYPE.STATIC.ordinal()) {

            View taskPlannedTimesView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.gs_view_task_planned_times, parent, false);

            contentBody.addView(taskPlannedTimesView, 0);

            return new StaticTaskViewHolder(itemView);
        }

        if (viewType == GSTask.TASK_TYPE.ALARM.ordinal()) {

            View taskPlannedTimesView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.gs_view_task_central_and_date, parent, false);

            contentBody.addView(taskPlannedTimesView, 0);

            return new AlarmTaskViewHolder(itemView, defaultRemoveItemCallback);
        }

        if (viewType == GSTask.TASK_TYPE.REGULAR.ordinal() || viewType == GSTask.TASK_TYPE.RAID.ordinal()) {

            View taskPlannedTimesView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.gs_view_task_planned_times, parent, false);

            contentBody.addView(taskPlannedTimesView, 0);

            return new RegularTaskViewHolder(itemView, defaultRemoveItemCallback, fragmentManager);
        }

        if (viewType == GSTask.TASK_TYPE.DISTRICTWATCH.ordinal()) {

            View taskPlannedTimesView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.gs_view_task_times_visited, parent, false);

            contentBody.addView(taskPlannedTimesView, 0);

            return new DistrictWatchTaskViewHolder(itemView, defaultRemoveItemCallback);
        }

        return holder;
    }

    public void removeItemAt(int position) {
        getItems().remove(position);
        notifyItemRemoved(position);
        // TODO RecyclerView bug https://github.com/lucasr/twoway-view/issues/134
//        if (position == 0) {
//            notifyDataSetChanged();
//        } else {
//            notifyItemRemoved(position);
//        }
    }


    private class UpdateTaskStateAsync extends AsyncTask<Void, Void, GSTask.TASK_STATE> {

        private final T task;
        private final TaskViewHolder holder;
        private final GSTask.TASK_STATE from_state;
        private final boolean isNew;

        public UpdateTaskStateAsync(T task, TaskViewHolder holder, GSTask.TASK_STATE from_state, boolean isNew) {
            this.task = task;
            this.holder = holder;
            this.from_state = from_state;
            this.isNew = isNew;
        }

        public UpdateTaskStateAsync(T task, TaskViewHolder holder, boolean isNew) {
            this.task = task;
            this.holder = holder;
            this.from_state = GSTask.TASK_STATE.PENDING;
            this.isNew = isNew;
        }


        @Override
        protected GSTask.TASK_STATE doInBackground(Void... voids) {
            // might contain LDS lookup
            return task.getTaskState();
        }

        @Override
        protected void onPostExecute(GSTask.TASK_STATE to_state) {


            holder.updateTaskState(context, from_state, to_state, isNew, isNew);

            super.onPostExecute(to_state);
        }
    }


    ;

//    private void setDistanceValue(GSTask task, Location deviceLocation, LinearLayout distanceLayout, TextView distanceType, TextView distanceValue) {
//
//        if (deviceLocation == null) {
//            return;
//        }
//
//        ParseGeoPoint targetGeoPoint = task.getPosition();
//        ParseModule.DistanceStrings distanceStrings = ParseModule.distanceBetweenString(
//                deviceLocation, targetGeoPoint);
//        distanceType.setText(distanceStrings.distanceType);
//        distanceValue.setText(distanceStrings.distanceValue);
//
//
//        Set<GSTask> allGeofenced = tasksCache.getAllGeofencedTasks();
//        Set<GSTask> within = tasksCache.getWithinGeofence();
//        Set<GSTask> outside = tasksCache.getOutsideGeofence();
//
//        TextView[] colorViews = new TextView[]{distanceType, distanceValue};
//        if (within.contains(task)) {
//            setDistanceColor(R.color.button_success_gradient_dark, colorViews);
//        } else if (outside.contains(task)) {
//            setDistanceColor(R.color.button_warning_gradient_dark, colorViews);
//        } else if (allGeofenced.contains(task)) {
//            setDistanceColor(R.color.button_info_gradient_dark, colorViews);
//        } else {
//            setDistanceColor(R.color.button_inverse_disabled, colorViews);
//        }
//
//        if (distanceLayout.getVisibility() == View.GONE) {
//            distanceLayout.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void setDistanceColor(int color, TextView[] views) {
//        int colorRes = context.getResources().getColor(color);
//        for (TextView v: views) {
//            v.setTextColor(colorRes);
//        }
//    }


}
