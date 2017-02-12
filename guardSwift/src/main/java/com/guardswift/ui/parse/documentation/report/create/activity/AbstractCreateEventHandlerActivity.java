package com.guardswift.ui.parse.documentation.report.create.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventViewPagerFragment;
import com.guardswift.util.Analytics;
import com.guardswift.util.ToastHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class AbstractCreateEventHandlerActivity extends
        InjectingAppCompatActivity implements OnPageChangeListener, AddEventHandler {

//    public interface AddEventCallback {
//        public void addEvent(String type, int amount, String newClientLocation,
//                             String location, String remark);
//    }


    protected static final String TAG = AbstractCreateEventHandlerActivity.class
            .getSimpleName();

    private static final String STATE_PAGE = "com.guardswift.ui.fragments.event.STATE_PAGE";


//    @Inject
//    ParseObjectFactory parseObjectFactory;
//    @Inject
//    LocalData localData;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.btn_previous)
    Button btnPrevious;
    @Bind(R.id.btn_next)
    Button btnNext;

    @Inject
    GSTasksCache taskCache;
    @Inject
    GuardCache guardCache;
    @Inject
    EventTypeCache eventTypeCache;

    private AddEventViewPagerFragment fragment;
    private ActionBar actionBar;
    private Bundle eventBundle;
    private int currentPage;
//    private Guard mGuard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gs_activity_create_event);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

//        onPrepareFieldVariables();


//        actionBar.setDisplayUseLogoEnabled(false);

//        onCreateActionBar(actionBar);

        eventBundle = new Bundle();

//        if (getIntent().hasExtra(EXTRA_BUNDLE)) {
//            eventBundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
//        }


//        mGuard = guardCache.getLoggedIn();

        if (savedInstanceState == null) {
            fragment = AddEventViewPagerFragment.newInstance(this, getClient());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_layout, fragment, "addEventViewpagerFragment")
                    .commit();

            eventTypeCache.clearSelected();
        } else {
            eventBundle = savedInstanceState.getBundle(EXTRA_EVENT_BUNDLE);
            currentPage = savedInstanceState.getInt(STATE_PAGE, 0);

            fragment = (AddEventViewPagerFragment) getSupportFragmentManager().findFragmentByTag("addEventViewpagerFragmen");
            fragment.setPage(currentPage);
        }

        onPageSelected(currentPage);

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.previousPage();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.nextPageDelayed();
                if (fragment.isLastPage()) {
                    verifyEventTimestampAndSave();
                }
            }
        });
    }

    private void verifyEventTimestampAndSave() {
        if (taskCache.getLastSelected().isWithinScheduledTime()) {
            saveEvent(new Date());
            return;
        }

        ToastHelper.toast(this, getString(R.string.verify_event_time));

        final DateTime timestamp = new DateTime();
        RadialTimePickerDialogFragment timePickerDialog = RadialTimePickerDialogFragment
                .newInstance(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                                 @Override
                                 public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                                     final Calendar cal = Calendar.getInstance();
                                     cal.setTime(timestamp.toDate());
                                     cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                     cal.set(Calendar.MINUTE, minute);

                                     saveEvent(cal.getTime());
                                 }
                             }, timestamp.getHourOfDay(), timestamp.getMinuteOfHour(),
                        DateFormat.is24HourFormat(this));
        timePickerDialog.show(this.getSupportFragmentManager(), "FRAG_TAG_TIME_PICKER");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle eventBundle = data.getBundleExtra(EXTRA_EVENT_BUNDLE);
            switch (requestCode) {
                case UpdateEventHandler.REQUEST_EVENT_TYPE:
                    setEventType(eventBundle.getString(EXTRA_EVENT_TYPE));
                    break;
                case UpdateEventHandler.REQUEST_EVENT_PEOPLE:
                    setPeople(eventBundle.getString(EXTRA_PEOPLE));
                    break;
                case UpdateEventHandler.REQUEST_EVENT_LOCATIONS:
                    setLocations(eventBundle.getString(EXTRA_LOCATIONS));
                    break;
                case UpdateEventHandler.REQUEST_EVENT_REMARKS:
                    setRemarks(eventBundle.getString(EXTRA_REMARKS));
                    break;
            }

            fragment.updateVisibilityCurrentPage();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onPageSelected(int position) {
        // update bottom toolbar
        currentPage = position;

        btnPrevious.setEnabled(true);
        btnPrevious.setText(getString(R.string.navigate_previous));

        btnNext.setEnabled(true);
        btnNext.setText(getString(R.string.navigate_next));

        if (position == 0) {
            btnPrevious.setEnabled(false);
        }
        if (fragment.isLastPage()) {
            btnNext.setText(getString(R.string.save));
        }
    }

    ;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_PAGE, currentPage);
        outState.putBundle(EXTRA_EVENT_BUNDLE, eventBundle);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (actionBar != null) {
            actionBar.setSubtitle(getClient().getFullAddress());
        }
    }

    //    abstract void onPrepareFieldVariables();

//    abstract void onCreateActionBar(ActionBar actionBar);

    private Client getClient() {
        return taskCache.getLastSelected().getClient();
    }

    ;

    abstract void saveEvent(Date timestamp, String event, int amount, String people, String clientLocation,
                            String remarks);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateBackOrUp();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        navigateBackOrUp();
    }


    private void navigateBackOrUp() {
        finish();
//        NavUtils.navigateUpFromSameTask(this);
    }

    public void setEventType(String eventType) {
        eventBundle.putString(EXTRA_EVENT_TYPE, eventType);
    }

    public void setEventType(EventType event) {
        setEventType(event.getName());

//        boolean nextpage = !event.hasAmount();
//        if (!nextpage) {
//            new CommonDialogsBuilder.BetterPicks(getSupportFragmentManager()).enterEventAmount(getEventType(), new NumberPickerDialogFragment.NumberPickerDialogHandler() {
//                @Override
//                public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
//                    selectAmount(String.valueOf(number));
//                }
//            }).show();
//            return;
//        }
        fragment.nextPageDelayed();
    }

    private void selectAmount(String amountString) {
        int amount = 0;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
//            ((AbstractCreateEventActivity) getActivity()).setAmount(0, true);
        }
        setAmount(amount, true);
    }

    @Override
    public String getEventType() {
        return eventBundle.getString(EXTRA_EVENT_TYPE, "");
    }

    @Override
    public void setAmount(int amount) {
        setAmount(amount, false);
    }

    public void setAmount(int amount, boolean nextpage) {
        eventBundle.putInt(EXTRA_AMOUNT, amount);
        if (nextpage)
            fragment.nextPageDelayed();
    }

    @Override
    public int getAmount() {
        return eventBundle.getInt(EXTRA_AMOUNT, 0);
    }

    @Override
    public void setPeople(String people) {
        eventBundle.putString(EXTRA_PEOPLE, people);
    }

    @Override
    public String getPeople() {
        return eventBundle.getString(EXTRA_PEOPLE);
    }

    @Override
    public void setLocations(String location) {
        eventBundle.putString(EXTRA_LOCATIONS, location);
    }

    @Override
    public String getLocations() {
        return eventBundle.getString(EXTRA_LOCATIONS);
    }

    @Override
    public void setRemarks(String remarks) {
        eventBundle.putString(EXTRA_REMARKS, remarks);
    }

    @Override
    public String getRemarks() {
        return eventBundle.getString(EXTRA_REMARKS);
    }

    public Bundle getEventBundle() {
        return eventBundle;
    }

    public void saveEvent(Date timestamp) {

        String event_type = eventBundle.getString(EXTRA_EVENT_TYPE);
        int amount = eventBundle.getInt(EXTRA_AMOUNT);
        final String people = eventBundle.getString(EXTRA_PEOPLE, "");
        final String clientLocation = eventBundle.getString(EXTRA_LOCATIONS, "");
        final String remarks = eventBundle.getString(EXTRA_REMARKS, "");

        saveEvent(timestamp, event_type, amount, people, clientLocation, remarks);

        GSTask task = taskCache.getLastSelected();
        // store remark tokens
        if (remarks != null && !remarks.isEmpty() && task.getTaskType() != GSTask.TASK_TYPE.STATIC) {

            final EventType eventType = eventTypeCache.getSelected();
            final Client client = getClient();
            EventRemark.getQueryBuilder(true)
                    .matching(client).matching(eventType)
                    .build().findInBackground(new FindCallback<EventRemark>() {

                @Override
                public void done(List<EventRemark> eventRemarks, ParseException e) {
                    if (e != null) {
                        return;
                    }

                    int autocompletesCount = 0;
                    String[] remarkTokens = remarks.split(",");
                    for (String remarkToken : remarkTokens) {
                        String remark = remarkToken.trim();

                        boolean exists = false;
                        for (EventRemark eventRemark : eventRemarks) {
                            if (eventRemark.getRemark().equals(remark)) {
                                exists = true;
                            }
                        }

                        if (!exists) {
                            EventRemark.create(eventType, clientLocation, remark, getClient(), guardCache.getLoggedIn()).pinThenSaveEventually();
                        } else {
                            autocompletesCount++;
                        }
                    }

                    Analytics.eventEventLogTrend(Analytics.CreateEventlogAction.Autocomplete, null, autocompletesCount);
                }
            });


        }

        finish();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    ;

    public void onPageScrollStateChanged(int state) {
    }

    ;

}
