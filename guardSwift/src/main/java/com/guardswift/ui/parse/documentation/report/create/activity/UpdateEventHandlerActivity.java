package com.guardswift.ui.parse.documentation.report.create.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.documentaion.EventLogCache;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventLocationFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventPeopleFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventRemarkFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventTypeFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.EventEntryFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateEventHandlerActivity extends InjectingAppCompatActivity implements UpdateEventHandler {

    private static final String TAG = UpdateEventHandlerActivity.class.getSimpleName();

    private static final String EVENT_REQUEST = "com.guardswift.EVENT_REQUEST";
    private static final String MODIFY_SELECTED_EVENTLOG = "com.guardswift.MODIFY_SELECTED_EVENTLOG";


    /**
     * Starts a new instance that direclty modifies the passed EventLog 
     * 
     * @param context
     * @param eventLog
     * @param eventRequest
     */
    public static void newInstance(Context context, EventLog eventLog, int eventRequest) {

        String preselected = "";
        switch (eventRequest) {
            case REQUEST_EVENT_TYPE:
                preselected = eventLog.getEvent();
                break;
            case REQUEST_EVENT_PEOPLE:
                preselected = eventLog.getPeople();
                break;
            case REQUEST_EVENT_LOCATIONS:
                preselected = eventLog.getLocations();
                break;
            case REQUEST_EVENT_REMARKS:
                preselected = eventLog.getRemarks();
                break;
            default:
                throw new IllegalArgumentException("Unknown request type");
        }
        Client client = eventLog.getClient();

        ParseCacheFactory parseCacheFactory = GuardSwiftApplication.getInstance().getCacheFactory();
        parseCacheFactory.getEventLogCache().setSelected(eventLog);
        parseCacheFactory.getClientCache().setSelected(client);

        context.startActivity(prepareIntent(context, eventRequest, preselected));
    }
    
    /**
     * Starts a new instance that simply allows selecting a new value and returns it as a result
     * This means that the parent Activity should be ready to receive the result in onActivityForResult
     * to do something with the newly selected value
     * 
     * @param activity
     * @param client
     * @param eventRequest
     * @param preselected
     */
    public static void newInstance(FragmentActivity activity, Client client, int eventRequest, String preselected) {
        ParseCacheFactory parseCacheFactory = GuardSwiftApplication.getInstance().getCacheFactory();
        parseCacheFactory.getClientCache().setSelected(client);
        activity.startActivityForResult(prepareIntent(activity, eventRequest, preselected), eventRequest);
    }
    
    private static Intent prepareIntent(Context context, int eventRequest, String preselected) {
        Intent i = new Intent(context, UpdateEventHandlerActivity.class);
        // if the calling context is prepared to handle the result, do not save selected eventlog
        i.putExtra(MODIFY_SELECTED_EVENTLOG, !(context instanceof AddEventHandler));
        // pass along which event information is the user updating
        i.putExtra(EVENT_REQUEST, eventRequest);
        // a single bundle is used to pass the event entries and hold pre-selected
        i.putExtra(EXTRA_EVENT_BUNDLE, preparePreselectBundle(eventRequest, preselected));
        
        return i;
    }

    private static Bundle preparePreselectBundle(int eventRequest, String preselected) {
        Bundle b = new Bundle();
        switch (eventRequest) {
            case REQUEST_EVENT_TYPE:
                b.putString(EXTRA_EVENT_TYPE, preselected);
                break;
            case REQUEST_EVENT_PEOPLE:
                b.putString(EXTRA_PEOPLE, preselected);
                break;
            case REQUEST_EVENT_LOCATIONS:
                b.putString(EXTRA_LOCATIONS, preselected);
                break;
            case REQUEST_EVENT_REMARKS:
                b.putString(EXTRA_REMARKS, preselected);
                break;
            default:
                throw new IllegalArgumentException("Unknown request type");
        }
        return b;
    }

    @Inject
    ClientCache clientCache;
    @Inject
    EventLogCache eventLogCache;
    @Inject
    EventTypeCache eventTypeCache;
    @Inject
    FragmentManager fragmentManager;

    ActionBar actionBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.content)
    LinearLayout container;

    @BindView(R.id.footer)
    LinearLayout footer;


    Bundle eventBundle;

    private int eventRequest;
    private boolean modifySelectedEventLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gs_activity_toolbar_footer);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            //noinspection RestrictedApi
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        createSaveButton();

        Client client = clientCache.getSelected();
        EventType eventType = eventTypeCache.getSelected();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EVENT_REQUEST)) {

            modifySelectedEventLog = intent.getBooleanExtra(MODIFY_SELECTED_EVENTLOG, false);

            eventBundle = intent.getBundleExtra(EXTRA_EVENT_BUNDLE);

            Fragment fragment = null;
            eventRequest = intent.getIntExtra(EVENT_REQUEST, 0); 
            switch (eventRequest) {
                case REQUEST_EVENT_TYPE:
                    fragment = AddEventTypeFragment.newInstance(client);
                    break;
                case REQUEST_EVENT_PEOPLE:
                    fragment = AddEventPeopleFragment.newInstance(client);
                    break;
                case REQUEST_EVENT_LOCATIONS:
                    fragment = AddEventLocationFragment.newInstance(client);
                    break;
                case REQUEST_EVENT_REMARKS:
                    fragment = AddEventRemarkFragment.newInstance(client, eventType);
                    break;
            }
            if (fragment != null && fragment instanceof EventEntryFragment) {
                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
                EventEntryFragment eventEntryFragment = ((EventEntryFragment) fragment);
                int title = eventEntryFragment.getTitle();
                if (actionBar != null) {
                    actionBar.setTitle(title);
                    actionBar.setSubtitle(clientCache.getSelected().getFullAddress());
                }
            }
        }
    }


    private void createSaveButton() {
        Button saveBtn = new Button(this, null, android.R.style.Widget_ActionButton);
        saveBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
        {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            saveBtn.setHeight(actionBarHeight);
        }
        saveBtn.setGravity(Gravity.CENTER);
        saveBtn.setTypeface(null, Typeface.BOLD);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResultAndFinish(RESULT_OK);
            }
        });
        saveBtn.setText(R.string.save);
        footer.addView(saveBtn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResultAndFinish(RESULT_CANCELED);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        setResultAndFinish(RESULT_CANCELED);
    }


    /**
     * Set eventBundle as result and finish
     *
     * @param resultCode
     */
    private void setResultAndFinish(int resultCode) {
        Log.d(TAG, "setResultAndFinish: " + (resultCode == RESULT_OK) + " " + modifySelectedEventLog);
        if (resultCode == RESULT_OK && modifySelectedEventLog) {
            EventLog eventLog = eventLogCache.getSelected();

            switch (eventRequest) {
                case REQUEST_EVENT_TYPE:
                    eventLog.put(EventLog.event, eventBundle.getString(EXTRA_EVENT_TYPE, ""));
                    break;
                case REQUEST_EVENT_PEOPLE:
                    eventLog.put(EventLog.people, eventBundle.getString(EXTRA_PEOPLE, ""));
                    break;
                case REQUEST_EVENT_LOCATIONS:
                    eventLog.put(EventLog.clientLocation, eventBundle.getString(EXTRA_LOCATIONS, ""));
                    break;
                case REQUEST_EVENT_REMARKS:
                    eventLog.put(EventLog.remarks, eventBundle.getString(EXTRA_REMARKS, ""));
                    break;
            }

            eventLog.pinThenSaveEventually();
        }
        setResult(resultCode, getIntent().putExtra(EXTRA_EVENT_BUNDLE, eventBundle));
        finish();
    }


    @Override
    public void setEventType(EventType eventType) {
        eventBundle.putString(EXTRA_EVENT_TYPE, eventType.getName());
        setResultAndFinish(RESULT_OK);
    }

    @Override
    public void setAmount(int amount) {
        // not used
    }

    @Override
    public void setPeople(String people) {
        eventBundle.putString(EXTRA_PEOPLE, people);
    }

    @Override
    public void setLocations(String clientLocations) {
        eventBundle.putString(EXTRA_LOCATIONS, clientLocations);
    }
    @Override
    public void setRemarks(String remarks) {
        eventBundle.putString(EXTRA_REMARKS, remarks);
    }

    @Override
    public String getEventType() {
        return eventBundle.getString(EXTRA_EVENT_TYPE);
    }

    @Override
    public int getAmount() {
        return 0; // not used
    }

    @Override
    public String getPeople() {
        return eventBundle.getString(EXTRA_PEOPLE);
    }

    @Override
    public String getLocations() {
        return eventBundle.getString(EXTRA_LOCATIONS);
    }


    @Override
    public String getRemarks() {
        return eventBundle.getString(EXTRA_REMARKS);
    }
}
