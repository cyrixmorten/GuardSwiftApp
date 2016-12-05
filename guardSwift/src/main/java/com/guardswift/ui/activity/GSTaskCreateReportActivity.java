package com.guardswift.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.documentation.eventlog.AbstractEventFragment;
import com.guardswift.ui.parse.documentation.eventlog.CircuitUnitEventFragment;
import com.guardswift.ui.parse.documentation.eventlog.DistrictWatchClientEventFragment;
import com.guardswift.ui.parse.documentation.eventlog.TaskEventFragment;

import javax.inject.Inject;

public class GSTaskCreateReportActivity extends InjectingAppCompatActivity {

    public static final String MODE_ADD_EVENT = "MODE_ADD_EVENT";
    public static final String HAS_ADD_EVENT_BUTTON = "HAS_ADD_BUTTON";

    public static final String FILTER_EVENT = "com.guardswift.FILTER_EVENT";

    public static final String TASK_TYPE = "com.guardswift.TASK_TYPE";


    public static void start(Context context, GSTask task) {

        GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().setSelected(task);

        context.startActivity(new Intent(context,
                GSTaskCreateReportActivity.class)
                .putExtra(GSTaskCreateReportActivity.TASK_TYPE, task.getTaskType()));
    }


    @Inject
    android.support.v7.app.ActionBar actionBar;

    @Inject
    ParseCacheFactory parseCacheFactory;

    private AbstractEventFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_empty);

            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(getString(R.string.title_events));

        if (getIntent().hasExtra(TASK_TYPE)) {
            GSTask.TASK_TYPE frag_type = (GSTask.TASK_TYPE) getIntent().getSerializableExtra(TASK_TYPE);

            switch (frag_type) {
                case ALARM:
                    fragment = TaskEventFragment.newInstance(parseCacheFactory.getTaskCache().getSelected());
                    break;

                case REGULAR:
                    fragment = CircuitUnitEventFragment.newInstance(this, parseCacheFactory.getCircuitUnitCache().getSelected());
                    break;

                case DISTRICTWATCH:
                    fragment = DistrictWatchClientEventFragment.newInstance(this, parseCacheFactory.getDistrictWatchClientCache().getSelected());
                    break;

//                case ALARM:
//                    fragment = AlarmEventFragment.newInstance(this, parseCacheFactory.getAlarmCache().getSelected());
//                    break;
            }


            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();

        } else {
            Toast.makeText(this, "Missing fragment type!", Toast.LENGTH_LONG).show();
            finish();
        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.filter, menu);
//        filterMenu = menu.findItem(R.id.menu_filter);
//        filterMenu.setEnabled(false);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

//            case R.id.menu_filter:
//                        SingleChoiceDialogFragment.newInstance(true, R.string.filter_by_event, filterEvents, new SingleChoiceDialogFragment.SingleChoiceDialogCallback() {
//                            @Override
//                            public void singleChoiceDialogItemSelected(int index, String value) {
//                                fragment.applySearch(value);
//                            }
//                        }).show(getSupportFragmentManager(), "dialog_filter");
//                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
