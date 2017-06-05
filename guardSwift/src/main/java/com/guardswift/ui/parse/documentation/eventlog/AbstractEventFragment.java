package com.guardswift.ui.parse.documentation.eventlog;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.crashlytics.android.Crashlytics;
import com.guardswift.R;
import com.guardswift.dagger.InjectingListFragment;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.guardswift.util.Analytics;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public abstract class AbstractEventFragment extends InjectingListFragment {

    protected static final String TAG = AbstractEventFragment.class
            .getSimpleName();


    //    protected static final String FILTER_EVENT = "FILTER_EVENT";
    protected static final String FILTER_EXCLUDE_PERIMITER = "FILTER_EXCLUDE_PERIMITER";
    protected static final String FILTER_EXCLUDE_AUTOMATIC = "FILTER_EXCLUDE_AUTOMATIC";


    public AbstractEventFragment() {
    }

    private EventAdapter mAdapter;


    @BindView(R.id.btn_header)
    Button btn_addevent;
    @BindView(R.id.loading)
    ProgressBar loading;

    abstract ParseQuery<EventLog> getEventLogQuery(List<String> filterEvents, boolean excludePerimiterEvents, boolean excludeAutomaticEvent);

    abstract void openAddEvent();

    abstract GSTask.TASK_TYPE getFragmentType();

    abstract GSTask getTaskPointer();

    boolean excludePerimiter;
    boolean excludeAutomatic;

    private MenuItem filterMenu;
    private List<String> eventTypes;
    private List<String> filterEvents = new ArrayList<>();
    private Integer[] filterIndexes = new Integer[0];

    private Client mClient;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


//        mClient = (getTaskPointer() != null) ? getTaskPointer().getClient() : null;
//        if (mClient != null) {
//            new EventLog.QueryBuilder(true).matching(mClient).excludeAutomatic().whereIsReportEntry().build().countInBackground(new CountCallback() {
//                @Override
//                public void done(int count, ParseException e) {
//                    if (count == 0) {
//                        Log.e(TAG, "UPDATING EVENTLOGS");
//                        new EventLog().updateDatastore(getTaskPointer());
//                    }
//                }
//            });
//        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filter, menu);
        filterMenu = menu.findItem(R.id.menu_filter);
        filterMenu.setEnabled(false);

        super.onCreateOptionsMenu(menu, inflater);

        fetchEventTypes();

    }

    private void fetchEventTypes() {
        if (mClient == null) {
            return;
        }

        EventType.getQueryBuilder(true).matchingIncludes(mClient).sortByTimesUsed().build().findInBackground(new FindCallback<EventType>() {
            @Override
            public void done(List<EventType> eventTypes, ParseException e) {
                if (e != null) {
                    Crashlytics.logException(e);
                    return;
                }

                if (eventTypes.isEmpty()) {
                    return;
                }

                AbstractEventFragment.this.eventTypes = new ArrayList<String>();
                for (EventType event : eventTypes) {
                    AbstractEventFragment.this.eventTypes.add(event.getName());
                }

                if (filterMenu != null) {
                    filterMenu.setEnabled(true);
                }


            }
        });
    }

    private Integer[] getSelectedEventTypeFilters() {
        List<Integer> selections = new ArrayList<>();
        for (String eventType : eventTypes) {
            if (filterEvents.contains(eventType)) {
                selections.add(eventTypes.indexOf(eventType));
            }
        }
        Integer[] selectionsArray = selections.toArray(new Integer[selections.size()]);
        Log.d(TAG, "Filter: " + Arrays.toString(selectionsArray));
        return selectionsArray;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.filter_by_event)
                        .items(eventTypes.toArray(new CharSequence[eventTypes.size()]))
                        .itemsCallbackMultiChoice(getSelectedEventTypeFilters(), new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] filters) {
                                /**
                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected check box to actually be selected.
                                 * See the limited multi choice dialog example in the sample project for details.
                                 **/

                                Log.d(TAG, Arrays.toString(which));
                                Log.d(TAG, Arrays.toString(filters));

                                filterEvents.clear();
                                for (CharSequence filter : filters) {
                                    filterEvents.add(filter.toString());
                                }


                                applySearch(filterEvents);

                                return true;
                            }
                        })
                        .negativeText(android.R.string.cancel)
                        .positiveText(android.R.string.ok)
                        .show();

//                SingleChoiceDialogFragment.newInstance(true, R.string.filter_by_event, eventTypes, new SingleChoiceDialogFragment.SingleChoiceDialogCallback() {
//                    @Override
//                    public void singleChoiceDialogItemSelected(int index, String value) {
//                        applySearch(value);
//                        Analytics.eventEventLogTrend(Analytics.CreateEventlogAction.Filter, value, 1);
//                    }
//                }).show(getChildFragmentManager(), "dialog_filter");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void applySearch(final List<String> filterEvents) {
//        applySearch(filterEvents, whereIsReportEntry, excludeAutomatic);
        applySearch(filterEvents, true, true);
    }

    public void applySearch(final List<String> filterEvents, final boolean excludePerimiterEvents, final boolean excludeAutomaticEvent) {

        Log.d(TAG, "applySearch " + excludePerimiterEvents + " " + excludeAutomaticEvent);

        mAdapter = new EventAdapter(getActivity(),
                new ParseQueryAdapter.QueryFactory<EventLog>() {

                    @Override
                    public ParseQuery<EventLog> create() {
                        return getEventLogQuery(filterEvents, excludePerimiterEvents, excludeAutomaticEvent);
                    }
                });


        mAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<EventLog>() {
            @Override
            public void onLoading() {
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaded(List<EventLog> eventLogs, Exception e) {
                if (loading != null)
                    loading.setVisibility(View.GONE);

            }
        });

        setListAdapter(mAdapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.listview_selectable_with_button_header, container,
                false);


        unbinder = ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();

        btn_addevent.setText(getString(R.string.add_event));
        if (!arguments.getBoolean(GSTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON)) {
            btn_addevent.setVisibility(View.GONE);
        }

        // filters
        String filterEvent = arguments.getString(GSTaskCreateReportActivity.FILTER_EVENT);
        excludePerimiter = arguments.getBoolean(FILTER_EXCLUDE_PERIMITER);
        excludeAutomatic = arguments.getBoolean(FILTER_EXCLUDE_AUTOMATIC);

//        applySearch(filterEvent, whereIsReportEntry, excludeAutomatic);
        applySearch(new ArrayList<String>(), true, true);


        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments().getBoolean(GSTaskCreateReportActivity.MODE_ADD_EVENT)) {
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EventLog eventLog = mAdapter.getItem(position);
                    showCreateEventDialog(eventLog);
                }
            });
        } else if (getArguments().getBoolean(GSTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON)) {
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EventLog eventLog = mAdapter.getItem(position);
                    // go to details view
                    Intent intent = new Intent(getActivity(),
                            GSTaskCreateReportActivity.class)
                            .putExtra(GSTaskCreateReportActivity.FILTER_EVENT, eventLog.getEvent())
                            .putExtra(GSTaskCreateReportActivity.TASK_TYPE, getFragmentType())
                            .putExtra(GSTaskCreateReportActivity.MODE_ADD_EVENT, true)
                            .putExtra(GSTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON, true);

                    startActivity(intent);


                }
            });
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void showCreateEventDialog(final EventLog eventLog) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_adapter_item_event_log, null);
        view.setBackgroundResource(android.R.color.transparent);

        TextView event = ButterKnife.findById(view, R.id.event);
        LinearLayout amountLayout = ButterKnife.findById(view, R.id.amountLayout);
        final TextView amount = ButterKnife.findById(view, R.id.amount);
        TextView location = ButterKnife.findById(view, R.id.location);
        TextView remarks = ButterKnife.findById(view, R.id.remarks);

        TextView time = ButterKnife.findById(view, R.id.time);
        TextView guard = ButterKnife.findById(view, R.id.guard);
        TextView relativeTime = ButterKnife.findById(view, R.id.relativeTime);

        time.setVisibility(View.INVISIBLE);
        guard.setVisibility(View.INVISIBLE);
        relativeTime.setVisibility(View.INVISIBLE);

        final String amountString = (eventLog.getAmount() > 0) ? String.valueOf(eventLog.getAmount()) : "";

        event.setText(eventLog.getEvent());
        amount.setText(amountString);
        location.setText(eventLog.getLocations());
        remarks.setText(eventLog.getRemarks());

        amountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NumberPickerBuilder()

                        .setFragmentManager(getActivity().getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                        .addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
                            @Override
                            public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
                                amount.setText(String.valueOf(number));
                            }
                        })
                        .setLabelText(getString(R.string.amount))
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setDecimalVisibility(View.INVISIBLE)
                        .show();

//                new MaterialDialog.Builder(getActivity())
//                        .title(getString(R.string.amount))
//                        .positiveText(getString(android.R.string.ok))
//                        .negativeText(android.R.string.cancel)
//                        .inputType(InputType.TYPE_CLASS_NUMBER)
//                        .input(getString(R.string.amount), "", new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(MaterialDialog dialog, CharSequence input) {
//                                amount.setText(input);
//                            }
//                        }).show();
            }
        });


        new MaterialDialog.Builder(getActivity())
                .title(R.string.add_event)
                .positiveText(R.string.save)
//                .negativeText(R.string.edit)
                .negativeColor(getResources().getColor(android.R.color.holo_blue_light))
                .neutralText(android.R.string.cancel)
                .customView(view, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        new EventLog.Builder(getActivity()).from(eventLog, getTaskPointer())
                                .amount(amount.getText())
                                .saveAsync();
                        Analytics.eventEventLogTrend(Analytics.CreateEventlogAction.Copy);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                        CreateEventActivityFactory.start(getActivity(), eventLog);
                        Analytics.eventEventLogTrend(Analytics.CreateEventlogAction.Edit);
                    }
                })
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void onEventMainThread(UpdateUIEvent ev) {
        Object obj = ev.getObject();
        if (obj instanceof Location) {
            return;
        }

        if (mAdapter != null) {
            mAdapter.loadObjects();
        }
    }

    @OnClick(R.id.btn_header)
    public void add() {
        openAddEvent();

        Analytics.eventEventLogTrend(Analytics.CreateEventlogAction.New);
    }


}
