package com.guardswift.ui.parse.documentation.report.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportEditActivity extends InjectingAppCompatActivity {


    private static final String TAG = ReportEditActivity.class.getSimpleName();

    public static void start(Context context, GSTask gsTask) {

        ParseCacheFactory cacheFactory = GuardSwiftApplication.getInstance().getCacheFactory();
        cacheFactory.getTasksCache().setSelected(gsTask);

        context.startActivity(new Intent(context, ReportEditActivity.class));
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Inject
    GSTasksCache gsTasksCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gs_activity_toolbar);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        GSTask task = gsTasksCache.getLastSelected();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

            actionBar.setTitle(R.string.title_report);
            actionBar.setSubtitle(task.getClient().getFullAddress());
        }


        getSupportFragmentManager().beginTransaction().replace(R.id.content, ReportEditViewPagerFragment.newInstance(task)).commit();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
