package com.guardswift.ui.parse.data.tracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.guardswift.R;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.ui.menu.MenuItemBuilder;
import com.guardswift.ui.menu.MenuItemIcons;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

public class TrackerListFragment extends AbstractParseRecyclerFragment<Tracker, TrackerRecycleAdapter.TrackerViewHolder> {

    public static TrackerListFragment newInstance() {
        TrackerListFragment fragment = new TrackerListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Date fromDate = new Date();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setSearchDate(fromDate);
    }

    private void setSearchDate(Date date) {
        if (getActivity() != null && getActivity() instanceof  AppCompatActivity && isAdded()) {
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (actionBar != null) {
                actionBar.setSubtitle(
                        DateFormat.getLongDateFormat(getContext()).format(date)
                );
            }
        }

        if (!date.equals(fromDate)) {
            fromDate = date;
            reloadLocalData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        new MenuItemBuilder(getContext())
                .icon(MenuItemIcons.createWithFontAwesomeIcon(getContext(), FontAwesome.Icon.faw_calendar))
                .showAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                .addToMenu(menu, R.string.date, new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        DateTime dateTimeToday = new DateTime(fromDate);

                        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                                        Log.d(TAG, "year: "+year);
                                        Log.d(TAG, "monthOfYear: "+monthOfYear);
                                        Log.d(TAG, "dayOfMonth: "+dayOfMonth);

                                        setSearchDate(new DateTime()
                                                .withYear(year)
                                                .withMonthOfYear(monthOfYear + 1)
                                                .withDayOfMonth(dayOfMonth).toDate());
                                    }
                                })
                                .setFirstDayOfWeek(Calendar.SUNDAY)
                                .setPreselectedDate(dateTimeToday.year().get(), dateTimeToday.monthOfYear().get() - 1, dateTimeToday.dayOfMonth().get())
                                .setDateRange(null, new MonthAdapter.CalendarDay())
                                .setThemeDark();
                        cdp.show(getChildFragmentManager(), "FRAGMENT_DATE_PICKER");
                        return false;
                    }
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new Tracker();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<Tracker> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Tracker>() {
            @Override
            public ParseQuery<Tracker> create() {
                return new Tracker.QueryBuilder(false).build().whereLessThanOrEqualTo(Tracker.createdAt, fromDate).addDescendingOrder(Tracker.createdAt);
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Tracker, TrackerRecycleAdapter.TrackerViewHolder> createRecycleAdapter() {
        return new TrackerRecycleAdapter(getContext(), createNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return false;
    }
}
