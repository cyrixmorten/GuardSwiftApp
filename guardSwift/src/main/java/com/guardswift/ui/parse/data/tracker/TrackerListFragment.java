package com.guardswift.ui.parse.data.tracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.guardswift.R;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.query.TrackerQueryBuilder;
import com.guardswift.ui.menu.MenuItemBuilder;
import com.guardswift.ui.menu.MenuItemIcons;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.parse.ui.widget.ParseQueryAdapter;

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

    public TrackerListFragment() {
    }

    private Date fromDate = new Date();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
            updatedNetworkQuery();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        new MenuItemBuilder(getContext())
                .icon(MenuItemIcons.create(getContext(), FontAwesome.Icon.faw_calendar))
                .showAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                .addToMenu(menu, R.string.date, menuItem -> {

                    DateTime dateTimeToday = new DateTime(fromDate);

                    CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                            .setOnDateSetListener((dialog, year, monthOfYear, dayOfMonth) -> {

                                setSearchDate(new DateTime()
                                        .withYear(year)
                                        .withMonthOfYear(monthOfYear + 1)
                                        .withDayOfMonth(dayOfMonth).toDate());
                            })
                            .setFirstDayOfWeek(Calendar.MONDAY)
                            .setPreselectedDate(dateTimeToday.year().get(), dateTimeToday.monthOfYear().get() - 1, dateTimeToday.dayOfMonth().get())
                            .setDateRange(null, new MonthAdapter.CalendarDay())
                            .setThemeDark();
                    cdp.show(getChildFragmentManager(), "FRAGMENT_DATE_PICKER");
                    return false;
                });

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    protected ParseQueryAdapter.QueryFactory<Tracker> createNetworkQueryFactory() {
        return () -> new TrackerQueryBuilder(false).build().whereLessThanOrEqualTo(Tracker.createdAt, fromDate).addDescendingOrder(Tracker.createdAt);
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
