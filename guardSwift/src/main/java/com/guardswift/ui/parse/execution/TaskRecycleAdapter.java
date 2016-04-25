package com.guardswift.ui.parse.execution;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
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
import com.guardswift.R;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PositionedViewHolder;
import com.guardswift.ui.parse.data.checkpoint.CheckpointActivity;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditActivity;
import com.guardswift.ui.parse.documentation.report.view.ReportHistoryListFragment;
import com.guardswift.ui.parse.execution.circuit.TaskDescriptionActivity;
import com.guardswift.util.AnimationHelper;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQueryAdapter;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

import bolts.Task;
import butterknife.Bind;
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

        @Bind(R.id.timeStart)
        TextView vTimeStart;
        @Bind(R.id.timeEnd)
        TextView vTimeEnd;
        @Bind(R.id.aTvClock)
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
                        task.pinThenSaveEventually(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                task.addReportEntry(context, context.getString(R.string.started), new GetCallback<EventLog>() {
                                    @Override
                                    public void done(EventLog object, ParseException e) {
                                        ReportEditActivity.start(context, task);

                                        loadingDialog.cancel();
                                    }
                                });
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
    }


    public static class RegularTaskViewHolder extends TaskViewHolder<CircuitUnit> {

        @Bind(R.id.timeStart)
        TextView vTimeStart;
        @Bind(R.id.timeEnd)
        TextView vTimeEnd;


        public RegularTaskViewHolder(View v, RemoveItemCallback removeItemCallback) {
            super(v, removeItemCallback);
        }


        @Override
        public void onActionOpen(Context context, CircuitUnit task) {
            super.onActionOpen(context, task);

//            boolean inverseSelection = !selectedItems.get(getAdapterPosition(), false);
            boolean inverseSelection = vContentFooter.getVisibility() == View.GONE;

            // Save the selected positions to the SparseBooleanArray
            if (task.isStarted() || inverseSelection) {
                expandFooter();
            } else {
                collapseFooter();
            }

        }

        @Override
        public void onActionAbort(Context context, CircuitUnit task) {
            super.onActionAbort(context, task);
            collapseFooter();
        }

        @Override
        public void onActionFinish(final Context context, final CircuitUnit task) {
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

                            RegularTaskViewHolder.super.onActionFinish(context, task);

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
                    Fragment fragment = ReportHistoryListFragment.newInstance(task.getClient());
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
    }

    public static class DistrictWatchTaskViewHolder extends TaskViewHolder<DistrictWatchClient> {

        @Bind(R.id.timesVisited_actual)
        TextView vTimesVisited_actual;
        @Bind(R.id.timesVisited_expected)
        TextView vTimesVisited_expected;

        public DistrictWatchTaskViewHolder(View itemView, RemoveItemCallback removeItemCallback) {
            super(itemView, removeItemCallback);

            this.vBtnAborted.setVisibility(View.GONE);
            this.vBtnFinished.setVisibility(View.GONE);
            this.vClientNumber.setVisibility(View.GONE);
            this.vTaskDesc.setVisibility(View.GONE);

        }

        @Override
        public void onActionArrive(Context context, DistrictWatchClient task) {
            super.onActionArrive(context, task);

            int newTimesArrived = task.getTimesArrived() + 1;
            vTimesVisited_actual.setText(String.valueOf(newTimesArrived));

            if (newTimesArrived == task.getSupervisions()) {
                removeItemCallback.removeAt(getAdapterPosition());
            }
        }

    }


    public static class TaskViewHolder<T extends BaseTask> extends PositionedViewHolder implements TaskActionCallback<T> {


        @Bind(R.id.cardview)
        CardView cardview;

        @Bind({R.id.content_colorBorder_top, R.id.content_colorBorder_bottom})
        List<ImageView> vColorBorders;

        @Bind(R.id.task_state_arrived)
        BootstrapButton vBtnArrived;
        @Bind(R.id.task_state_aborted)
        BootstrapButton vBtnAborted;
        @Bind(R.id.task_state_finished)
        BootstrapButton vBtnFinished;

        @Bind(R.id.content_header)
        LinearLayout vContentHeader;
        @Bind(R.id.content_body)
        LinearLayout vContentBody;
        @Bind(R.id.content_footer)
        LinearLayout vContentFooter;

        @Bind(R.id.clientNumber)
        TextView vClientNumber;
        @Bind(R.id.clientName)
        TextView vName;
        @Bind(R.id.clientAddress)
        TextView vAddress;
        @Bind(R.id.taskTypeDesc)
        TextView vTaskDesc;

        // footer buttons
        @Bind(R.id.btn_view_report)
        Button vBtnViewReport;
        @Bind(R.id.btn_new_event)
        Button vBtnAddNewEvent;
        @Bind(R.id.btn_report_history)
        Button vBtnReportHistory;
        @Bind(R.id.btn_task_description)
        Button vBtnTaskdescription;
        @Bind(R.id.btn_client_info)
        Button vBtnClientContacts;
        @Bind(R.id.btn_checkpoints)
        Button vBtnCheckpoints;

        protected RemoveItemCallback removeItemCallback;

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


        }

        @Override
        public void onActionArrive(final Context context, final T task) {

            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_arrived, task.getClientName()), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    performTaskAction(context, task, ACTION.ARRIVE);
                }
            }).show();
        }

        @Override
        public void onActionAbort(final Context context, final T task) {
            new CommonDialogsBuilder.MaterialDialogs(context).okCancel(R.string.confirm_action, context.getString(R.string.mark_aborted, task.getClientName()), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    performTaskAction(context, task, ACTION.ABORT);
                }
            }).show();
        }

        @Override
        public void onActionFinish(Context context, T task) {
            performTaskAction(context, task, ACTION.FINISH);
        }


        public TaskViewHolder(View v, RemoveItemCallback removeItemCallback) {
            super(v);
            ButterKnife.bind(this, v);

            this.vBtnArrived.setBootstrapSize(DefaultBootstrapSize.LG);
            this.vBtnAborted.setBootstrapSize(DefaultBootstrapSize.LG);
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


            this.vBtnArrived.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActionArrive(context, task);
                }
            });

            this.vBtnAborted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActionAbort(context, task);
                }
            });


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
        protected <T extends BaseTask> Task<T> performTaskAction(final Context context, final T task, final ACTION action) {

            final Task<T>.TaskCompletionSource result = Task.create();

            final GSTask.TASK_STATE previousTaskState = task.getTaskState();
            final TaskController taskController = task.getController();
            if (taskController.canPerformAction(action, task)) {
                new AsyncTask<Void, Void, T>() {

                    @Override
                    protected void onPreExecute() {
                        GSTask.TASK_STATE toState = taskController.translatesToState(action);
                        updateTaskState(context, previousTaskState, toState);
                        super.onPreExecute();
                    }

                    @Override
                    protected T doInBackground(Void... voids) {
                        // might contain LDS lookup
                        return (T) taskController.performAction(action, task);
                    }

                    @Override
                    protected void onPostExecute(T updatedTask) {
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
            int color = getTaskStateColor(context, state);
            updateTaskStateButtons(context, state);
            for (View border : this.vColorBorders) {
                tintBackgroundColor(border, color, 100);
            }
            tintBackgroundColor(this.cardview, color, 50);
        }

        public void updateTaskState(Context context, GSTask.TASK_STATE fromState, GSTask.TASK_STATE toState) {
            if (toState == GSTask.TASK_STATE.FINSIHED) {
                if (removeItemCallback != null) {
                    removeItemCallback.removeAt(getAdapterPosition());
                }
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
            bootstrapActionButtonDefaults(context, this.vBtnAborted);
            bootstrapActionButtonDefaults(context, this.vBtnFinished);

            int colorRes = getTaskStateColorResource(state);
            switch (state) {
                case ARRIVED:
                    bootstrapActionButtonSelect(context, this.vBtnArrived, colorRes);
                    break;
                case ABORTED:
                    bootstrapActionButtonSelect(context, this.vBtnAborted, colorRes);
                    break;
                case FINSIHED:
                    bootstrapActionButtonSelect(context, this.vBtnFinished, colorRes);
                    bootstrapActionDisableAll();
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
            this.vBtnAborted.setEnabled(false);
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
                case FINSIHED:
                    color = R.color.bootstrap_gray_light;
                    break;
            }
            return color;
        }

        private <T extends View> void tintBackgroundColor(List<T> views, int colorFrom, int colorTo, final int alpha, int duration) {
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

        final T task = getItem(position);

        Client client = task.getClient();
        if (client != null) {
            holder.vClientNumber.setText((client.getNumber() != 0) ? String.valueOf(client.getNumber()) : "");
            holder.vName.setText(client.getName());
            holder.vAddress.setText(client.getFullAddress());
            holder.vBtnCheckpoints.setVisibility((client.hasCheckPoints()) ? View.VISIBLE : View.GONE);
            holder.vBtnClientContacts.setVisibility((!client.getContactsWithNames().isEmpty()) ? View.VISIBLE : View.GONE);
        }

        if (task instanceof StaticTask) {
            holder.vContentFooter.setVisibility(View.GONE);
            holder.vContentHeader.setVisibility(View.GONE);
            if (holder instanceof StaticTaskViewHolder) {
                StaticTaskViewHolder staticTaskViewHolder = (StaticTaskViewHolder) holder;
                Date timeStarted = ((StaticTask) task).getTimeArrived();
                if (timeStarted != null) {
                    staticTaskViewHolder.vTimeStart.setText(DateFormat.getLongDateFormat(context).format(timeStarted));
                    staticTaskViewHolder.vTimeEnd.setVisibility(View.GONE);
                    staticTaskViewHolder.iconClock.setVisibility(View.VISIBLE);
                } else {
                    staticTaskViewHolder.vTimeStart.setVisibility(View.GONE);
                    staticTaskViewHolder.vTimeEnd.setVisibility(View.GONE);
                    staticTaskViewHolder.iconClock.setVisibility(View.GONE);
                }

                staticTaskViewHolder.setupTaskActionButtons(context, (StaticTask) task);
            }
        }

        if (task instanceof CircuitUnit) {
            holder.vContentFooter.setVisibility((task.isStarted() || selectedItems.get(position, false)) ? View.VISIBLE : View.GONE);
            CircuitUnit circuitUnit = (CircuitUnit) task;
            if (holder instanceof RegularTaskViewHolder) {
                holder.vBtnTaskdescription.setVisibility((!circuitUnit.getDescription().isEmpty()) ? View.VISIBLE : View.GONE);

                RegularTaskViewHolder regularTaskViewHolder = (RegularTaskViewHolder) holder;
                regularTaskViewHolder.vTaskDesc.setText(circuitUnit.getName());
                regularTaskViewHolder.vTimeStart.setText(circuitUnit.getTimeStartString());
                regularTaskViewHolder.vTimeEnd.setText(circuitUnit.getTimeEndString());

                regularTaskViewHolder.setupTaskActionButtons(context, (CircuitUnit) task);
            }
        }

        if (task instanceof DistrictWatchClient) {
            holder.vContentFooter.setVisibility(View.GONE);
            DistrictWatchClient districtWatchClient = (DistrictWatchClient) task;
            // swap name and address to enhance the address
            holder.vName.setText(districtWatchClient.getFullAddress());
            holder.vAddress.setText(districtWatchClient.getClientName());
            if (holder instanceof DistrictWatchTaskViewHolder) {
                DistrictWatchTaskViewHolder districtWatchTaskViewHolder = ((DistrictWatchTaskViewHolder) holder);
                districtWatchTaskViewHolder.vTimesVisited_actual.setText(String.valueOf(districtWatchClient.getTimesArrived()));
                districtWatchTaskViewHolder.vTimesVisited_expected.setText(String.valueOf(districtWatchClient.getSupervisions()));
                if (districtWatchClient.getTimesArrived() == districtWatchClient.getSupervisions()) {
                    holder.vBtnArrived.setEnabled(false);
                }

                districtWatchTaskViewHolder.setupTaskActionButtons(context, (DistrictWatchClient) task);
            }
        }


        new PositionedViewHolder.CalcDistanceAsync(task, holder).execute();

        holder.setTaskState(context, task);

//        new UpdateTaskStateAsync(task, holder, isNew).execute();
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
            removeItemAt(position);
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

        if (viewType == GSTask.TASK_TYPE.REGULAR.ordinal()) {

            View taskPlannedTimesView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.gs_view_task_planned_times, parent, false);

            contentBody.addView(taskPlannedTimesView, 0);

            return new RegularTaskViewHolder(itemView, defaultRemoveItemCallback);
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
