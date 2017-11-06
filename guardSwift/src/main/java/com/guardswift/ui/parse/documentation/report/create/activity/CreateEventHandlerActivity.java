package com.guardswift.ui.parse.documentation.report.create.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventViewPagerFragment;
import com.guardswift.util.Analytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.guardswift.persistence.parse.documentation.event.EventLog.clientLocation;

public class CreateEventHandlerActivity extends
        InjectingAppCompatActivity implements OnPageChangeListener, AddEventHandler {


    protected static final String TAG = CreateEventHandlerActivity.class
            .getSimpleName();

    public static void start(Context context, ParseTask task) {
        GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().setSelected(task);

        context.startActivity(new Intent(context, CreateEventHandlerActivity.class));
    }

    private static final String STATE_PAGE = "com.guardswift.ui.fragments.event.STATE_PAGE";


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btn_previous)
    Button btnPrevious;
    @BindView(R.id.btn_next)
    Button btnNext;

    @Inject
    ParseTasksCache taskCache;
    @Inject
    GuardCache guardCache;
    @Inject
    EventTypeCache eventTypeCache;

    private AddEventViewPagerFragment fragment;
    private ActionBar actionBar;
    private Bundle eventBundle;
    private int currentPage;

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


        eventBundle = new Bundle();


        if (savedInstanceState == null || fragment == null) {
            fragment = AddEventViewPagerFragment.newInstance(getClient());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_layout, fragment, "addEventViewpagerFragment")
                    .commit();

            eventTypeCache.clearSelected();
        } else {
            eventBundle = savedInstanceState.getBundle(EXTRA_EVENT_BUNDLE);
            currentPage = savedInstanceState.getInt(STATE_PAGE, 0);

            fragment = (AddEventViewPagerFragment) getSupportFragmentManager().findFragmentByTag("addEventViewpagerFragment");
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
                    saveEvent();
                }
            }
        });
    }


    // Called by UpdateEventHandlerActivity
    // Used when updating EventLog from Summary page
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

        if (getClient() == null) {
            finish();
            return;
        }

        if (actionBar != null) {
            actionBar.setSubtitle(getClient().getFullAddress());
        }
    }

    private Client getClient() {
        return taskCache.getLastSelected().getClient();
    }

    ;


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
    }

    public void setEventType(String eventType) {
        eventBundle.putString(EXTRA_EVENT_TYPE, eventType);
    }

    public void setEventType(EventType event) {
        setEventType(event.getName());

        fragment.nextPageDelayed();
    }

    private void selectAmount(String amountString) {
        int amount = 0;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException ignored) {
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


    public void saveEvent() {

        String event_type = eventBundle.getString(EXTRA_EVENT_TYPE);
        int amount = eventBundle.getInt(EXTRA_AMOUNT);
        String people = eventBundle.getString(EXTRA_PEOPLE, "");
        String clientLocation = eventBundle.getString(EXTRA_LOCATIONS, "");

        final String remarks = eventBundle.getString(EXTRA_REMARKS, "");
        final ParseTask task = taskCache.getLastSelected();

        new EventLog.Builder(this)
                .taskPointer(task, ParseTask.EVENT_TYPE.OTHER)
                .event(event_type)
                .amount(amount)
                .people(people)
                .location(clientLocation)
                .remarks(remarks)
                .eventCode(task.getEventCode())
                .deviceTimeStamp(new Date())
                .saveAsync(new GetCallback<EventLog>() {
                    @Override
                    public void done(EventLog object, ParseException e) {
                        finish();
                    }
                }, new GetCallback<EventLog>() {
                    @Override
                    public void done(EventLog object, ParseException e) {
                        storeRemarkTokens(task, remarks);
                    }
                });
    }

    private void storeRemarkTokens(ParseTask task, final String remarks) {


        if (remarks != null && !remarks.isEmpty() && task.getTaskType() != ParseTask.TASK_TYPE.STATIC) {

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
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    ;

    public void onPageScrollStateChanged(int state) {
    }

    ;

}
