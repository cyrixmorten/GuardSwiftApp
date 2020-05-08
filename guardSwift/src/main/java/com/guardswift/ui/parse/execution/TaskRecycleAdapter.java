package com.guardswift.ui.parse.execution;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.cardview.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.helpers.ViewHelper;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PositionedViewHolder;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditViewPagerFragment;
import com.guardswift.ui.parse.documentation.report.view.ReportHistoryListFragment;
import com.guardswift.util.AnimationHelper;
import com.guardswift.util.OpenLocalPDF;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.parse.GetCallback;
import com.parse.ui.widget.ParseQueryAdapter;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import static com.guardswift.core.tasks.controller.TaskController.ACTION;
import static com.guardswift.ui.helpers.ViewHelper.TintBackground.tintBackgroundColor;


public class TaskRecycleAdapter extends ParseRecyclerQueryAdapter<ParseTask, TaskRecycleAdapter.TaskViewHolder> {

    private static final String TAG = TaskRecycleAdapter.class.getSimpleName();

    public interface OnOpenTaskListener {
        void openTask(ParseTask task);
    }

    /**
     * Overrides default task open action if set
     * Also hides task event actions (pending, arrive, finish, etc)
     */
    private OnOpenTaskListener openTaskListener;

    public void setOpenTaskListener(OnOpenTaskListener openTaskListener) {
        this.openTaskListener = openTaskListener;
    }

    public static class StaticTaskViewHolder extends TaskViewHolder {

        @BindView(R.id.timeStart)
        TextView vTimeStart;
        @BindView(R.id.timeEnd)
        TextView vTimeEnd;
        @BindView(R.id.aTvClock)
        AwesomeTextView iconClock;

        StaticTaskViewHolder(View v) {
            super(v, null);

            vTimeEnd.setVisibility(View.GONE);
        }

        @Override
        public void onActionOpen(final Context context, final ParseTask task) {
            super.onActionOpen(context, task);

            if (task.isPending()) {
                new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.static_guarding_report, context.getString(R.string.begin_static_guarding_at_client, task.getClientName()), (materialDialog, dialogAction) -> {
                    // show indeterminate progress
                    final MaterialDialog loadingDialog = new CommonDialogsBuilder.MaterialDialogs(context).indeterminate(R.string.report_creating).show();

                    Guard guard = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().getLoggedIn();
                    task.setStartedBy(guard);

                    task.saveInBackground().continueWith(taskResult -> {
                        Exception error = taskResult.getError();
                        if (error != null) {
                            loadingDialog.cancel();
                            new CommonDialogsBuilder.MaterialDialogs(context).missingInternetContent().show();

                            new HandleException(context, TAG, "Failed to start static task", error);
                            return null;
                        }

                        task.addReportEntry(context, context.getString(R.string.started), null, (object, e) -> {
                            GenericToolbarActivity.start(context,
                                    context.getString(R.string.title_report),
                                    task.getClientAddress(),
                                    ReportEditViewPagerFragment.newInstance(task));

                            loadingDialog.cancel();
                        });

                        return null;

                    });
                }).show();
            } else {
                GenericToolbarActivity.start(context,
                        context.getString(R.string.title_report),
                        task.getClientAddress(),
                        ReportEditViewPagerFragment.newInstance(task));
            }

        }

        @Override
        protected void setupTaskActionButtons(final Context context, final ParseTask task) {
            this.cardview.setOnClickListener(view -> onActionOpen(context, task));
        }

        @Override
        public void update(Context context, ParseTask task) {
            super.update(context, task);

            if (task.isStaticTask()) {
                vContentFooter.setVisibility(View.GONE);
                vContentHeader.setVisibility(View.GONE);

                Date timeStarted = task.getCreatedAt();
                if (timeStarted != null) {
                    vTimeStart.setText(DateFormat.getLongDateFormat(context).format(timeStarted));
                    vTimeEnd.setVisibility(View.GONE);
                    iconClock.setVisibility(View.VISIBLE);
                } else {
                    vTimeStart.setVisibility(View.GONE);
                    vTimeEnd.setVisibility(View.GONE);
                    iconClock.setVisibility(View.GONE);
                }

                setupTaskActionButtons(context, task);
            }
            super.update(context, task);
        }
    }

    public static class AlarmTaskViewHolder extends TaskViewHolder {

        @BindView(R.id.tv_central)
        TextView vCentral;
        @BindView(R.id.tv_date)
        TextView vDate;


        AlarmTaskViewHolder(View v, RemoveItemCallback removeItemCallback) {
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


        @Override
        public void onActionFinish(final Context context, final ParseTask task) {

            // arrived
            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_finished), (materialDialog, dialogAction) -> AlarmTaskViewHolder.super.onActionFinish(context, task)).show();

        }


        @Override
        protected void setupTaskActionButtons(final Context context, final ParseTask task) {
            super.setupTaskActionButtons(context, task);

            vBtnViewReport.setOnClickListener(view -> GenericToolbarActivity.start(context,
                    context.getString(R.string.title_report),
                    task.getClient().getFullAddress(),
                    ReportEditViewPagerFragment.newInstance(task)));

            vBtnAddNewEvent.setOnClickListener(view -> CreateEventHandlerActivity.start(context, task));

            vBtnReportHistory.setOnClickListener(view -> {
                Fragment fragment = ReportHistoryListFragment.newInstance(task.getClient(), ParseTask.TASK_TYPE.ALARM);
                GenericToolbarActivity.start(
                        context,
                        context.getString(R.string.reports),
                        task.getClient().getFullAddress(),
                        fragment);
            });

            vBtnClientContacts.setOnClickListener(view -> new CommonDialogsBuilder.MaterialDialogs(context).clientContacts(task.getClient()).show());
        }

        @Override
        public void update(final Context context, final ParseTask task) {
            super.update(context, task);

            if (task.isRegularTask() || task.isRaidTask()) {
                vBtnPending.setVisibility(View.VISIBLE);
            }

            if (task.isAlarmTask()) {
                vBtnPending.setVisibility(View.GONE);

                if (task.isPending()) {
                    vBtnAccepted.setVisibility(View.VISIBLE);
                    vBtnArrived.setVisibility(View.GONE);
                    vBtnFinished.setVisibility(View.GONE);
                }

                if (task.isAborted() || task.isFinished()) {
                    vBtnArrived.setVisibility(View.GONE);
                }

                String description = context.getText(R.string.security_level) + ": " + task.getPriority() + "\n" +
                        context.getText(R.string.keybox) + ": " + task.getKeybox() + "\n" +
                        context.getText(R.string.remarks) + ": " + task.getRemarks();

                vTaskDesc.setText(description);

                String centralName = task.getCentralName();


                vCentral.setText(centralName);
                vDate.setText(
                        new SpannableStringBuilder()
                                .append(DateFormat.getDateFormat(context).format(task.getCreatedAt()))
                                .append(" ")
                                .append(DateFormat.getTimeFormat(context).format(task.getCreatedAt()))
                );

                setupTaskActionButtons(context, task);


                // TODO make smarter
                if (task.getCentralName().equals("G4S")) {
                    View centralButton = vContentFooter.findViewById(R.id.button_central_pdf);
                    if (centralButton == null) {
                        Button g4spdf = new Button(new ContextThemeWrapper(context, android.R.style.Widget_DeviceDefault_Button_Borderless), null, android.R.style.Widget_DeviceDefault_Button_Borderless);
                        g4spdf.setId(R.id.button_central_pdf);
                        g4spdf.setText(context.getString(R.string.alarm_panels));
                        g4spdf.setOnClickListener(view -> new OpenLocalPDF(context, "G4S").execute());
                        vContentFooter.addView(g4spdf);
                    }
                }

                View centralButton = vContentFooter.findViewById(R.id.button_original_alarm_text);
                if (centralButton == null) {
                    Button originalAlarm = new Button(new ContextThemeWrapper(context, android.R.style.Widget_DeviceDefault_Button_Borderless), null, android.R.style.Widget_DeviceDefault_Button_Borderless);
                    originalAlarm.setId(R.id.button_original_alarm_text);
                    originalAlarm.setText(context.getString(R.string.alarm_original_text));
                    originalAlarm.setOnClickListener(view -> new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.alarm, task.getOriginal()).show());
                    vContentFooter.addView(originalAlarm);
                }

            }
        }


    }

    public static class RegularTaskViewHolder extends TaskViewHolder {

        @BindView(R.id.info)
        TextView vInfo;
        @BindView(R.id.layout_info)
        LinearLayout vInfoLayout;
        @BindView(R.id.timeStart)
        TextView vTimeStart;
        @BindView(R.id.timeEnd)
        TextView vTimeEnd;


        private FragmentManager fragmentManager;

        RegularTaskViewHolder(View v, RemoveItemCallback removeItemCallback, FragmentManager fragmentManager) {
            super(v, removeItemCallback);

            this.fragmentManager = fragmentManager;
        }


        @Override
        public void onActionOpen(Context context, ParseTask task) {
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
        public void onActionAbort(Context context, ParseTask task) {
            super.onActionAbort(context, task);
            collapseFooter();
        }

        @Override
        public void onActionFinish(final Context context, final ParseTask task) {
            confirmIfMissingSupervisions(context, task, (object, e) -> {
                // Either not missing supervisions, or finish despite
                finishWithChecks(context, task);
            });
        }

        private void finishWithChecks(final Context context, final ParseTask task) {
            if (task.getTimesArrived() == 0 && fragmentManager != null) {
                missingArrivalTimestampDialog(context, (dialog, which) ->
                        addArrivalEvent(context, task, (object, e) ->
                                RegularTaskViewHolder.super.onActionFinish(context, task)));
            } else {
                Log.e(TAG, "Should show missing arrival dialog but had no fragment manager");
                RegularTaskViewHolder.super.onActionFinish(context, task);
            }
        }

        private void confirmIfMissingSupervisions(final Context context, final ParseTask task, final GetCallback<ParseTask> okCallback) {
            int plannedSupervision = task.getPlannedSupervisions();
            int timesArrived = task.getTimesArrived();
            int diff = plannedSupervision - timesArrived;

            if (plannedSupervision > 1 && diff != 0) {
                new CommonDialogsBuilder.MaterialDialogs(context).okCancel(
                        R.string.missing_supervisions,
                        context.getString(R.string.confirm_finish_missing_supervisions, diff),
                        (dialog, which) -> okCallback.done(task, null))
                        .canceledOnTouchOutside(false)
                        .show();
            } else {
                okCallback.done(task, null);
            }
        }


        private void addArrivalEvent(final Context context, final ParseTask task, final GetCallback<ParseTask> callback) {
            final DateTime timestamp = new DateTime();

            RadialTimePickerDialogFragment timePickerDialog = new RadialTimePickerDialogFragment()
                    .setStartTime(timestamp.getHourOfDay(), timestamp.getMinuteOfHour())
                    .setOnTimeSetListener((dialog, hourOfDay, minute) -> {

                        new EventLog.Builder(context)
                                .taskPointer(task, ParseTask.EVENT_TYPE.ARRIVE)
                                .event(context.getString(R.string.event_arrived))
                                .automatic(false)
                                .deviceTimeStamp(task.getArrivalDate(hourOfDay, minute))
                                .eventCode(EventLog.EventCodes.REGULAR_ARRIVED).saveAsync();


                        callback.done(task, null);
                    })
                    .setThemeDark()
                    .setForced24hFormat();

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

        private Task<Boolean> confirmExtraTime(Context context, int registeredExtraTime) {
            if (registeredExtraTime == 0) {
                return Task.forResult(true);
            }

            final TaskCompletionSource<Boolean> booleanTaskCompletionSource = new TaskCompletionSource<>();

            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action,
                    context.getString(R.string.confirm_extra_time, registeredExtraTime),
                    (dialog, action) -> booleanTaskCompletionSource.setResult(true),
                    (dialog, action) -> booleanTaskCompletionSource.setResult(false)).show();

            return booleanTaskCompletionSource.getTask();
        }

        private void extraTimeDialog(final Context context, final ParseTask task) {
            new MaterialDialog.Builder(context)
                    .title(R.string.extra_time_spend)
                    .items(R.array.extra_time_minutes_strings)
                    .itemsCallbackSingleChoice(0, (dialog, view, index, value) -> {
                        String[] extra_time_values = context.getResources()
                                .getStringArray(
                                        R.array.extra_time_minutes_values);
                        int minutes = Integer
                                .parseInt(extra_time_values[index]);
                        if (minutes != 0) {
                            new EventLog.Builder(context)
                                    .taskPointer(task, ParseTask.EVENT_TYPE.OTHER)
                                    .event(context.getString(R.string.event_extra_time))
                                    .amount(minutes)
                                    .remarks(value.toString())
                                    .eventCode(EventLog.EventCodes.REGULAR_EXTRA_TIME).saveAsync();
                        }


                        return true;
                    })
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .show();
        }

        @Override
        protected void setupTaskActionButtons(final Context context, final ParseTask task) {
            super.setupTaskActionButtons(context, task);

            vBtnViewReport.setOnClickListener(view -> GenericToolbarActivity.start(context,
                    context.getString(R.string.title_report),
                    task.getClientAddress(),
                    ReportEditViewPagerFragment.newInstance(task)));

            vBtnAddNewEvent.setOnClickListener(view -> CreateEventHandlerActivity.start(context, task));

            vBtnReportHistory.setOnClickListener(view -> {
                Fragment fragment = ReportHistoryListFragment.newInstance(task.getClient(), ParseTask.TASK_TYPE.REGULAR);
                GenericToolbarActivity.start(
                        context,
                        context.getString(R.string.reports),
                        task.getClientAddress(),
                        fragment);
            });

//            vBtnTaskdescription.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    TaskDescriptionActivity.start(contextWeakReference, task);
//                }
//            });


            vBtnClientContacts.setOnClickListener(view -> new CommonDialogsBuilder.MaterialDialogs(context).clientContacts(task.getClient()).show());
        }

        public void update(final Context context, final ParseTask task) {
            super.update(context, task);

            if (task.isRegularTask() || task.isRaidTask()) {
//                vBtnTaskdescription.setVisibility((!task.getDescription().isEmpty()) ? View.VISIBLE : View.GONE);

                vTaskDesc.setText(task.getName());
                vTimeStart.setText(task.getTimeStartString());
                vTimeEnd.setText(task.getTimeEndString());

                vTimeStart.setTextColor(task.isAfterScheduledStartTime() ? Color.GRAY : Color.RED);
                vTimeEnd.setTextColor(task.isBeforeScheduledEndTime() ? Color.GRAY : Color.RED);

                if (vInfoLayout.getChildCount() > 1) {
                    vInfoLayout.removeViewAt(0);
                }

                ImageView iconView = new ImageView(context);
                GoogleMaterial.Icon icon = (task.isRaidTask()) ? GoogleMaterial.Icon.gmd_car : GoogleMaterial.Icon.gmd_walk;
                iconView.setImageDrawable(new IconicsDrawable(context)
                        .icon(icon)
                        .color(Color.DKGRAY)
                        .sizeDp(24));

                iconView.setPadding(0, 0, 12, 0);

                vInfoLayout.addView(iconView, 0);

                String infoText = context.getString(R.string.times_supervised,
                        task.getTimesArrived(),
                        task.getPlannedSupervisions());

                vInfo.setText(infoText);

                vBtnAddExtraTime.setVisibility(View.VISIBLE);
                vBtnAddExtraTime.setOnClickListener(view -> {
                    MaterialDialog progressDialog = new CommonDialogsBuilder.MaterialDialogs(context).indeterminate().show();
                    task.getExtraMinutes().onSuccessTask((minutesTask) -> {
                        progressDialog.dismiss();

                        return confirmExtraTime(context, minutesTask.getResult());
                    }, Task.UI_THREAD_EXECUTOR).onSuccess((extraTimeTask) -> {
                        boolean showExtraTimeDialog = extraTimeTask.getResult();

                        Log.d(TAG, "showExtraTimeDialog: " + showExtraTimeDialog);

                        if (showExtraTimeDialog) {
                            extraTimeDialog(context, task);
                        }

                        return null;
                    }, Task.UI_THREAD_EXECUTOR).continueWith((boltsTask) -> {
                        if (boltsTask.getError() != null) {
                            new HandleException(context, TAG, "Error getting extra minutes", boltsTask.getError());
                        }

                        return null;
                    });

                });

                setupTaskActionButtons(context, task);

                if (task.isCompletedButNotMarkedFinished()) {
                    updateTaskState(context, ParseTask.TASK_STATE.ACCEPTED);
                }
            }

            if (!task.isRegularTask() && !task.isRaidTask()) {
                vBtnAddExtraTime.setVisibility(View.GONE);
            }
        }
    }


    public static class TaskViewHolder extends PositionedViewHolder {


        @BindView(R.id.cardview)
        CardView cardview;

        @BindViews({R.id.content_colorBorder_top, R.id.content_colorBorder_bottom})
        List<ImageView> vColorBorders;

        @BindView(R.id.task_state_active)
        BootstrapButton vBtnPending;
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
        //        @BindView(R.id.btn_task_description)
//        Button vBtnTaskdescription;
        @BindView(R.id.btn_client_info)
        Button vBtnClientContacts;


        RemoveItemCallback removeItemCallback;

        public void update(Context context, ParseTask task) {
            tvGuardName.setText((task.getGuard() != null) ? task.getGuard().getName() : "");
            setTaskState(context, task);
        }

        public void onActionOpen(Context context, ParseTask task) { }

        void expandFooter() {
            AnimationHelper.expand(vContentFooter);
            selectedItems.put(getAdapterPosition(), true);
        }

        void collapseFooter() {
            AnimationHelper.collapse(vContentFooter);
            selectedItems.delete(getAdapterPosition());
        }

        public void onActionAccept(final Context context, ParseTask task) {

            performTaskAction(context, task, ACTION.ACCEPT);


            vBtnAccepted.setVisibility(View.GONE);
            vBtnArrived.setVisibility(View.VISIBLE);

//            if (!task.getTaskType().equals(ParseTask.TASK_TYPE.ALARM)) {
//                vBtnAborted.setVisibility(View.VISIBLE);
//            }

            vBtnFinished.setVisibility(View.VISIBLE);

        }

        void onActionPending(final Context context, final ParseTask task) {

            if (ParseModule.distanceToMeters(task.getPosition()) <= task.getRadius()) {
                new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.manual_pending, context.getString(R.string.manual_pending_distance, task.getRadius())).show();
                return;
            }

            performTaskAction(context, task, ACTION.PENDING);
        }

        public void onActionArrive(final Context context, final ParseTask task) {

            new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.manual_arrival, context.getString(R.string.manual_arrival_no_longer_possible)).show();

        }

        public void onActionAbort(final Context context, ParseTask task) {
            performTaskAction(context, task, ACTION.ABORT);
        }

        public void onActionFinish(Context context, ParseTask task) {
            performTaskAction(context, task, ACTION.FINISH);
        }


        TaskViewHolder(View v, RemoveItemCallback removeItemCallback) {
            super(v);
            ButterKnife.bind(this, v);

            setRemoveItemCallback(removeItemCallback);
        }

        protected void setupTaskActionButtons(final Context context, final ParseTask task) {


            if (!this.cardview.hasOnClickListeners()) {
                this.cardview.setOnClickListener(view -> onActionOpen(context, task));
            }

            this.vBtnAccepted.setOnClickListener(view -> onActionAccept(context, task));
            this.vBtnPending.setOnClickListener(view -> onActionPending(context, task));
            this.vBtnArrived.setOnClickListener(view -> onActionArrive(context, task));
            this.vBtnFinished.setOnClickListener(view -> onActionFinish(context, task));

        }


        void setRemoveItemCallback(RemoveItemCallback removeItemCallback) {
            this.removeItemCallback = removeItemCallback;
        }

        Task<ParseTask> performTaskAction(final Context context, final ParseTask task, final ACTION action) {

            final TaskCompletionSource<ParseTask> result = new TaskCompletionSource<>();

            final ParseTask.TASK_STATE previousTaskState = task.getTaskState();
            final TaskController taskController = task.getController();

            // may perform LDS and should perhaps be done in background somehow
            ParseTask updatedTask = taskController.performManualAction(action, task);

            updateTaskState(context, task, previousTaskState, task.getTaskState());

            result.setResult(updatedTask);

            return result.getTask();
        }


        void setTaskState(Context context, ParseTask task) {
            ParseTask.TASK_STATE state = task.getTaskState();
            updateTaskState(context, state);
        }

        void updateTaskState(Context context, ParseTask.TASK_STATE toState) {
            int color = getTaskStateColor(context, toState);
            updateTaskStateButtons(context, toState);
            for (View border : this.vColorBorders) {
                tintBackgroundColor(border, color, 100);
            }
            tintBackgroundColor(this.cardview, color, 50);
        }

        void removeTask() {
            if (removeItemCallback != null) {
                removeItemCallback.removeAt(getAdapterPosition());
            }
        }

        void updateTaskState(Context context, ParseTask task, ParseTask.TASK_STATE fromState, ParseTask.TASK_STATE toState) {

            boolean fromActiveState = fromState == ParseTask.TASK_STATE.PENDING || fromState == ParseTask.TASK_STATE.ACCEPTED || fromState == ParseTask.TASK_STATE.ARRIVED;
            boolean toFinishedState = toState == ParseTask.TASK_STATE.FINISHED || toState == ParseTask.TASK_STATE.ABORTED;

            boolean fromFinishedState = fromState == ParseTask.TASK_STATE.FINISHED || fromState == ParseTask.TASK_STATE.ABORTED;
            boolean toActiveState = toState == ParseTask.TASK_STATE.PENDING || toState == ParseTask.TASK_STATE.ACCEPTED || toState == ParseTask.TASK_STATE.ARRIVED;

            if (task.isAlarmTask() || task.isStaticTask()) {
                if (fromActiveState && toFinishedState) {
                    removeTask();
                    return;
                }
                if (fromFinishedState && toActiveState) {
                    removeTask();
                    return;
                }
            }
            updateTaskState(context, fromState, toState, false, true);
        }

        void updateTaskState(Context context, ParseTask.TASK_STATE fromState, ParseTask.TASK_STATE toState, boolean isNew, boolean animate) {
            updateTaskStateColor(context, fromState, toState, isNew, animate);
            updateTaskStateButtons(context, toState);
        }

        private void updateTaskStateButtons(Context context, ParseTask.TASK_STATE state) {
            bootstrapActionButtonDefaults(context, this.vBtnPending);
            bootstrapActionButtonDefaults(context, this.vBtnArrived);
//            bootstrapActionButtonDefaults(contextWeakReference, this.vBtnAborted);
            bootstrapActionButtonDefaults(context, this.vBtnFinished);

            int colorRes = getTaskStateColorResource(state);
            switch (state) {
                case PENDING:
                    bootstrapActionButtonSelect(context, this.vBtnPending, colorRes);
                    break;
                case ARRIVED:
                    bootstrapActionButtonSelect(context, this.vBtnArrived, colorRes);
                    break;
//                case ABORTED:
//                    bootstrapActionButtonSelect(contextWeakReference, this.vBtnAborted, colorRes);
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
            button.setEnabled(true);
            button.setTextColor(context.getResources().getColor(R.color.bootstrap_gray_lighter));
            button.setBackgroundResource(colorRes);
        }

        private void bootstrapActionDisableAll() {
            this.vBtnArrived.setEnabled(false);
//            this.vBtnAborted.setEnabled(false);
            this.vBtnFinished.setEnabled(false);
        }

        private void updateTaskStateColor(Context context, ParseTask.TASK_STATE fromState, ParseTask.TASK_STATE toState, boolean isNew, boolean animate) {

            if (fromState.equals(toState)) {
                return;
            }

            int prevStateColor = getTaskStateColor(context, fromState);
            int toColor = getTaskStateColor(context, toState);
            int tintDuration = (animate) ? 500 : 0;
            int fromColor = (isNew) ? Color.WHITE : prevStateColor;

//            Log.d(TAG, "updateTaskStateColor " + fromState + "->" + toState);
//            Log.d(TAG, "updateTaskStateColor " + fromColor + "->" + toColor);

            ViewHelper.TintBackground.tintBackgroundColor(this.vColorBorders, fromColor, toColor, 100, tintDuration);
            ViewHelper.TintBackground.tintBackgroundColor(this.cardview, fromColor, toColor, 50, tintDuration);

        }

        private int getTaskStateColor(Context context, ParseTask.TASK_STATE state) {
            int colorRes = getTaskStateColorResource(state);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getColor(colorRes);
            } else {
                return context.getResources().getColor(colorRes);
            }
        }

        private int getTaskStateColorResource(ParseTask.TASK_STATE state) {
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
                    color = R.color.bootstrap_brand_danger;
                    break;
            }
            return color;
        }
    }

    private int lastPosition = -1;
    private static SparseBooleanArray selectedItems = new SparseBooleanArray();

    public TaskRecycleAdapter(Context context, ParseQueryAdapter.QueryFactory<ParseTask> factory) {
        super(factory);
        this.context = context;
    }

    public TaskRecycleAdapter(Context context, FragmentManager fragmentManager, ParseQueryAdapter.QueryFactory<ParseTask> factory) {
        super(factory);
        this.context = context;
        this.fragmentManager = fragmentManager;
    }


    @Override
    public int getItemViewType(int position) {
        return getItem(position).getTaskType().ordinal();
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, final int position) {

        final ParseTask task = getItem(position);

        if (openTaskListener != null) {
            holder.cardview.setOnClickListener((v) -> openTaskListener.openTask(task));
        }

        holder.vClientNumber.setText(task.getClientId());
        holder.vName.setText(task.getClientName());
        holder.vAddress.setText(task.getClientAddress());

        Client client = task.getClient();
        if (client != null) {
            holder.vBtnClientContacts.setVisibility((!client.getContactsWithNames().isEmpty()) ? View.VISIBLE : View.GONE);
        }

        holder.vContentFooter.setVisibility((task.isArrived() || selectedItems.get(position, false)) ? View.VISIBLE : View.GONE);


        holder.update(context, task);


//        debugGeofenceStatus(task, holder);

        new PositionedViewHolder.CalcDistanceAsync(task, holder).execute();
//        new UpdateTaskStateAsync(task, holder, isNew).execute();
    }

    private void debugGeofenceStatus(ParseTask task, TaskViewHolder holder) {
//        if (!BuildConfig.DEBUG) {
//            return;
//        }

        LinearLayout linearLayout = holder.vContentBody.findViewById(R.id.layout_debug_geofence);
        if (linearLayout == null) {
            linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setId(R.id.layout_debug_geofence);

            holder.vContentBody.addView(linearLayout);
        }

        linearLayout.removeAllViews();


//        linearLayout.addView(isGeofenced(task));
//        linearLayout.addView(isWithinGeofence(task));
//        linearLayout.addView(isOutsideGeofence(task));
        linearLayout.addView(isWithinScheduledTime(task));

//        TextView tvTaskTypeString = new TextView(contextWeakReference);
//        tvTaskTypeString.setText(task.getTaskTypeString());
//        linearLayout.addView(tvTaskTypeString);
//
        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM HH:mm",
        //        Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getDefault());
        //TextView tvTaskType = new TextView(context);
        //tvTaskType.setText(dateFormat.format(task.getUpdatedAt()));
        //linearLayout.addView(tvTaskType);
    }

    private TextView isWithinScheduledTime(ParseTask task) {
        TextView tv = new TextView(context);
        String isAfterStart = task.isAfterScheduledStartTime() ? "yes" : "no";
        String isBeforeEnd = task.isBeforeScheduledEndTime() ? "yes" : "no";
        tv.setText(" After start: " + isAfterStart + " Before end: " + isBeforeEnd);
        tv.setTextColor((task.isAfterScheduledStartTime()) ? Color.GREEN : Color.RED);

        return tv;
    }



    /**
     * Here is the key method to apply the animation
     */
//    private void setAnimation(View viewToAnimate, int position) {
//        // If the bound view wasn't previously displayed on screen, it's animated
//        if (position > lastPosition) {
//            Animation animation = AnimationUtils.loadAnimation(contextWeakReference, android.R.anim.slide_in_left);
//            viewToAnimate.startAnimation(animation);
//            lastPosition = position;
//        }
//    }

    public interface RemoveItemCallback {
        void removeAt(int position);
    }

    private RemoveItemCallback defaultRemoveItemCallback = position -> {
        // saw crash reports due to java.lang.ArrayIndexOutOfBoundsException by getAdapterPosition() returning -1
        if (position >= 0) {
            removeItemAt(position);
        }
    };

    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.gs_card_task, parent, false);

        selectedItems = new SparseBooleanArray();


        TaskViewHolder holder = new TaskViewHolder(itemView, defaultRemoveItemCallback);

        LinearLayout contentBody = itemView.findViewById(R.id.content_body);

        int taskContentHeaderLayoutId = viewType == ParseTask.TASK_TYPE.ALARM.ordinal() ? R.layout.gs_view_task_central_and_date : R.layout.gs_view_task_planned_times;

        View taskPlannedTimesView = LayoutInflater.
                from(parent.getContext()).
                inflate(taskContentHeaderLayoutId, parent, false);

        contentBody.addView(taskPlannedTimesView, 0);

        if (openTaskListener != null) {
            itemView.findViewById(R.id.content_header).setVisibility(View.GONE);
            itemView.findViewById(R.id.task_distance).setVisibility(View.GONE);
        }

        if (viewType == ParseTask.TASK_TYPE.STATIC.ordinal()) {
            return new StaticTaskViewHolder(itemView);
        }
        if (viewType == ParseTask.TASK_TYPE.ALARM.ordinal()) {
            return new AlarmTaskViewHolder(itemView, defaultRemoveItemCallback);
        }
        if (viewType == ParseTask.TASK_TYPE.REGULAR.ordinal() || viewType == ParseTask.TASK_TYPE.RAID.ordinal()) {
            return new RegularTaskViewHolder(itemView, defaultRemoveItemCallback, fragmentManager);
        }


        return holder;
    }

    private void removeItemAt(int position) {
        getItems().remove(position);

        // TODO RecyclerView bug https://github.com/lucasr/twoway-view/issues/134
        if (position == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(position);
        }
    }


}
