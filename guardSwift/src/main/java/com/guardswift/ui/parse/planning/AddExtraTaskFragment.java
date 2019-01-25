package com.guardswift.ui.parse.planning;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapButtonGroup;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.menu.MenuItemBuilder;
import com.guardswift.ui.menu.MenuItemIcons;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.data.taskgroup.TaskGroupListFragment;
import com.guardswift.util.StringHelper;
import com.guardswift.util.ToastHelper;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddExtraTaskFragment extends InjectingFragment {

    private static final String TAG = AddExtraTaskFragment.class.getSimpleName();

    private static String ARGUMENT_TASK = "task";

    public static AddExtraTaskFragment newInstance() {
        return new AddExtraTaskFragment();
    }

    public static AddExtraTaskFragment newInstance(ParseTask task) {

        AddExtraTaskFragment fragment = new AddExtraTaskFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.button_group_task_type) BootstrapButtonGroup taskTypeGroup;
    @BindView(R.id.button_type_regular) BootstrapButton buttonTypeRegular;
    @BindView(R.id.button_type_raid) BootstrapButton buttonTypeRaid;
    @BindView(R.id.button_group_week_days) BootstrapButtonGroup weekDayGroup;
    @BindView(R.id.button_choose_taskgroup) BootstrapButton taskGroupButton;
    @BindView(R.id.button_choose_client) BootstrapButton clientButton;
    @BindView(R.id.button_supervisions) BootstrapButton superVisionsButton;
    @BindView(R.id.button_time_start) BootstrapButton timeStartButton;
    @BindView(R.id.button_time_end) BootstrapButton timeEndButton;
    @BindView(R.id.button_expire_date) BootstrapButton expireButton;
    @BindView(R.id.button_create_task) BootstrapButton saveTaskButton;

    private List<BootstrapButton> weekDayButtons;


    private HashSet<Integer> weekDays = new HashSet<>();

    private Unbinder unbinder;

    private ParseTask task;

    public AddExtraTaskFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        onTaskTypeChanged();
        addWeekDayButtons();

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(ARGUMENT_TASK)) {
            task = bundle.getParcelable(ARGUMENT_TASK);
            updateAllViewsFromTask();

            saveTaskButton.setText(R.string.action_save_changes);

            return;
        }

        task = new ParseTask();
    }


    private void updateAllViewsFromTask() {
        updateTaskGroupButton();
        updateClientButton();
        updateTaskTypeButtonGroup();
        updateWeekDaysButtonGroup();
        updateSupervisionsButton();
        updateTimeStartButton();
        updateTimeEndButton();
        updateExpireDateButton();
    }

    private void updateExpireDateButton() {
        Date expireDate = task.getExpireDate();
        if (expireDate != null) {
            updateButtonValue(expireButton, DateUtils.formatDateTime(
                    getContext(),
                    expireDate.getTime(),
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE), true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (task.getObjectId() != null) {
            new MenuItemBuilder(getContext())
                    .icon(MenuItemIcons.create(getContext(), GoogleMaterial.Icon.gmd_delete))
                    .showAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    .addToMenu(menu, R.string.delete, menuItem -> {

                        new CommonDialogsBuilder.MaterialDialogs(getActivity())
                                .okCancel(R.string.confirm_action, getString(R.string.delete_extra_task),
                                        (dialog, which) -> task.deleteInBackground(
                                                (e) -> finishIfGenericActivity()
                                        )
                                ).show();


                        return false;
                    });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_extra_task, container,
                false);

        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    void onTaskTypeChanged() {
        buttonTypeRegular.setOnCheckedChangedListener((bootstrapButton, isChecked) -> {
            if (isChecked) {
                task.setTaskType(ParseTask.TASK_TYPE.REGULAR);

                updateTaskTypeButtonGroup();
            }
        });

        buttonTypeRaid.setOnCheckedChangedListener((bootstrapButton, isChecked) -> {
            if (isChecked) {
                task.setTaskType(ParseTask.TASK_TYPE.RAID);

                updateTaskTypeButtonGroup();
            }
        });
    }

    @OnClick(R.id.button_choose_taskgroup) void selectTaskGroup(BootstrapButton button) {
        TaskGroupListFragment taskGroupListFragment = TaskGroupListFragment.newInstance((taskGroup) -> {
            task.setTaskGroup(taskGroup);

            updateTaskGroupButton();
        });
        GenericToolbarActivity.start(Objects.requireNonNull(getContext()), R.string.select_taskgroup, R.string.select_taskgroup_for_extra_task, taskGroupListFragment);
    }

    @OnClick(R.id.button_choose_client) void selectClient(BootstrapButton button) {
        ClientListFragment clientListFragment = ClientListFragment.newInstance(ClientQueryBuilder.SORT_BY.NAME);
        clientListFragment.setOnClientSelectedListener(client -> {
            task.setClient(client);

            updateClientButton();
        });
        GenericToolbarActivity.start(Objects.requireNonNull(getContext()), R.string.select_client, R.string.select_client_for_extra_task, clientListFragment);
    }

    @OnClick(R.id.button_supervisions) void setPlannedSupervisions(BootstrapButton button) {
        new CommonDialogsBuilder.BetterPicks(Objects.requireNonNull(getActivity()).getSupportFragmentManager()).enterEventAmount(getString(R.string.number_of_supervisions),
                (reference, number, decimal, isNegative, fullNumber) -> {
                    Integer supervisions = number.intValue();

                    task.setPlannedSupervisions(supervisions);

                    updateSupervisionsButton();
                }
        ).show();
    }


    @OnClick({R.id.button_time_start, R.id.button_time_end}) void setTimeStartAndTimeEnd(BootstrapButton button) {
        RadialTimePickerDialogFragment timePickerDialog = new RadialTimePickerDialogFragment()
                .setOnTimeSetListener((dialog, hourOfDay, minute) -> {
                    final Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);

                    Date date = cal.getTime();

                    if (button.getId() == R.id.button_time_start) {
                        task.setTimeStart(date);
                        updateTimeStartButton();
                    }

                    if (button.getId() == R.id.button_time_end) {
                        task.setTimeEnd(date);
                        updateTimeEndButton();
                    }

                })
                .setThemeDark()
                .setForced24hFormat();
        timePickerDialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "FRAG_TAG_TIME_PICKER");
    }

    @OnClick(R.id.button_expire_date) void setExpireDate(BootstrapButton button) {
        DateTime dateTimeToday = task.has(ParseTask.expireDate) ? new DateTime(task.getExpireDate()) : new DateTime();

        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener((dialog, year, monthOfYear, dayOfMonth) -> {

                    Date date = new DateTime()
                            .withYear(year)
                            .withMonthOfYear(monthOfYear + 1)
                            .withDayOfMonth(dayOfMonth).toDate();

                    task.setExpireDate(date);

                    updateExpireDateButton();

                })
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setPreselectedDate(dateTimeToday.year().get(), dateTimeToday.monthOfYear().get() - 1, dateTimeToday.dayOfMonth().get())
                .setDateRange(new MonthAdapter.CalendarDay(), null)
                .setThemeDark();
        cdp.show(getChildFragmentManager(), "FRAGMENT_DATE_PICKER");
    }

    @OnClick(R.id.button_create_task) void createTask() {
        boolean hasTaskGroup = task.getTaskGroup() != null;
        boolean hasClient = task.getClient() != null;
        boolean hasTaskType = task.getTaskType() != null;
        boolean hasDays = !weekDays.isEmpty();
        boolean hasSupervisions = task.getPlannedSupervisions() != 0;
        boolean hasStartTime = task.getTimeStart() != null;
        boolean hasEndTime = task.getTimeEnd() != null;
        boolean hasExpireDate = task.getExpireDate() != null;

        boolean hasAllElements = hasTaskGroup && hasClient && hasTaskType && hasDays && hasSupervisions && hasStartTime && hasEndTime && hasExpireDate;

        if (!hasAllElements) {
            new CommonDialogsBuilder.MaterialDialogs(getActivity()).ok(R.string.not_performed, getString(R.string.not_all_values_are_filled)).show();
            return;
        }

        MaterialDialog progressDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).indeterminate(R.string.creating_task).show();
        task.addDays(weekDays);
        task.saveInBackground(e -> {
            progressDialog.dismiss();

            if (e != null) {
                new CommonDialogsBuilder.MaterialDialogs(getActivity()).ok(R.string.error_an_error_occured, e.getMessage()).show();
                return;
            }

            finishIfGenericActivity();
        });
    }

    private void finishIfGenericActivity() {
        if (getActivity() instanceof GenericToolbarActivity) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        weekDayButtons = null;
    }

    private void addWeekDayButtons() {
        weekDayButtons = new ArrayList<>();
        weekDayGroup.removeAllViews();

        for (int i = 1; i <= 7; i++) {
            final int day = i % 7; // turn 7 into 0

            BootstrapButton weekDayCheckButton = new BootstrapButton(getContext());
            weekDayCheckButton.setTag(day);
            weekDayCheckButton.setText(StringHelper.weekDayName(day));

            weekDayCheckButton.setOnCheckedChangedListener((buttonView, isChecked) -> {
                int dayTag = (int) buttonView.getTag();
                if (isChecked) {
                    weekDays.add(dayTag);
                } else {
                    weekDays.remove(dayTag);
                }

                weekDayGroup.setBootstrapBrand(weekDays.size() > 0 ? DefaultBootstrapBrand.SUCCESS : DefaultBootstrapBrand.DANGER);
            });

            weekDayGroup.addView(weekDayCheckButton);

            weekDayButtons.add(weekDayCheckButton);
        }
    }

    private void updateButtonValue(BootstrapButton button, String text, boolean valid) {
        button.setText(text);
        button.setBootstrapBrand(valid ? DefaultBootstrapBrand.SUCCESS : DefaultBootstrapBrand.DANGER);
    }

    private void updateTimeEndButton() {
        Date timeEnd = task.getTimeEnd();
        if (timeEnd != null) {
            updateButtonValue(timeEndButton,
                    DateUtils.formatDateTime(getContext(), timeEnd.getTime(), DateUtils.FORMAT_SHOW_TIME),
                    true);
        }
    }

    private void updateTimeStartButton() {
        Date timeStart = task.getTimeStart();
        if (timeStart != null) {
            updateButtonValue(
                    timeStartButton,
                    DateUtils.formatDateTime(getContext(), timeStart.getTime(), DateUtils.FORMAT_SHOW_TIME),
                    true);
        }
    }

    private void updateSupervisionsButton() {
        int supervisions = task.getPlannedSupervisions();
        updateButtonValue(
                superVisionsButton,
                Integer.toString(supervisions), supervisions > 0);
    }

    private void updateWeekDaysButtonGroup() {
        List<Integer> runDays = task.getDays();
        if (task.getDays() != null) {
            for (BootstrapButton weekDayButton : weekDayButtons) {
                int day = (int) weekDayButton.getTag();

                if (runDays.contains(day)) {
                    weekDayButton.setSelected(true);

                    weekDayGroup.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
                }
            }
        }
    }

    private void updateTaskTypeButtonGroup() {
        ParseTask.TASK_TYPE type = task.getTaskType();
        if (type != null) {
            taskTypeGroup.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);

            if (!buttonTypeRegular.isSelected() && !buttonTypeRaid.isSelected()) {
                if (type.equals(ParseTask.TASK_TYPE.REGULAR)) {
                    buttonTypeRegular.setSelected(true);
                }
                if (type.equals(ParseTask.TASK_TYPE.RAID)) {
                    buttonTypeRaid.setSelected(true);
                }
            }
        }
    }

    private void updateClientButton() {
        Client client = task.getClient();
        if (client != null) {
            updateButtonValue(
                    clientButton,
                    client.getIdAndName(), true);
        }
    }

    private void updateTaskGroupButton() {
        TaskGroup taskGroup = task.getTaskGroup();
        if (taskGroup != null) {
            updateButtonValue(
                    taskGroupButton,
                    taskGroup.getName(),
                    true);

        }
    }
}
