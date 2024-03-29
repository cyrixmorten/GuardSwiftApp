package com.guardswift.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.guardswift.R;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.drawer.MainNavigationDrawer;
import com.guardswift.ui.drawer.ToolbarFragmentDrawerCallback;
import com.guardswift.ui.parse.execution.alarm.AlarmsViewPagerFragment;
import com.guardswift.util.Device;
import com.guardswift.util.Sounds;
import com.mikepenz.materialdrawer.Drawer;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends InjectingAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SELECT_ALARMS = "SELECT_ALARMS";


    @Inject
    GuardCache guardCache;
    @Inject
    TaskGroupStartedCache taskGroupStartedCache;
    @Inject
    ParseModule parseModule;
    @Inject
    FragmentManager fm;
    @Inject
    MainNavigationDrawer navigationDrawer;

    @Inject
    Device device;
    @Inject
    Sounds sounds;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ToolbarFragmentDrawerCallback mainDrawerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gs_activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (!shouldRedirectToOtherActivity()) {
            GuardSwiftApplication.getInstance().bootstrapParseObjectsLocally(this, guardCache.getLoggedIn()).onSuccess(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) {

                    initDrawer();

                    return null;
                }
            });

        }
    }


    public void initDrawer() {
        Log.i(TAG, "initDrawer");

        mainDrawerCallback = new ToolbarFragmentDrawerCallback(this, toolbar, R.id.content);
        mainDrawerCallback.setActionCallback(action -> {
            if (action == MainNavigationDrawer.DRAWER_LOGOUT) {
                showLogoutDialog();
            }
        });

        Drawer mainNavigationDrawer = navigationDrawer.create(this, toolbar, mainDrawerCallback);


        mainNavigationDrawer.openDrawer();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        // 4.5.0: Prevent alarm sound from continuously playing
        // Can happen when multiple alarms overlap
        if (sounds.isPlayingAlarmSound()) {
            sounds.stopAlarm();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        Log.i(TAG, "onPostResume");
        if (shouldRedirectToOtherActivity()) {
            startActivity(redirectToOtherActivityIntent());
        } else {
            EventBusController.postUIUpdate();

            if (getIntent().hasExtra(SELECT_ALARMS)) {
                mainDrawerCallback.selectItem(AlarmsViewPagerFragment.newInstance(), R.string.alarms);
                navigationDrawer.getDrawer().closeDrawer();
            }
        }
    }

    // preventing multiple calls of startActivity in redirectToOtherActivityIntent
    public boolean shouldRedirectToOtherActivity() {
        return redirectToOtherActivityIntent() != null;
    }


    public Intent redirectToOtherActivityIntent() {
        Intent intent = null;
        if (!guardCache.isLoggedIn()) {
            intent = new Intent(MainActivity.this,
                    GuardLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        navigationDrawer = null;
        mainDrawerCallback = null;

        try {
            super.onDestroy();
        } catch (NullPointerException npe) {
            // https://code.google.com/p/android/issues/detail?id=216157
            Log.i(TAG, "NPE: Bug workaround");
        }

    }


    private SweetAlertDialog logoutDialog;

    private void showLogoutDialog() {
        logoutDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.logout))
                .setContentText(getString(R.string.logout_confirm))
                .setConfirmText(getString(android.R.string.yes))
                .setCancelText(getString(android.R.string.no))
                .setConfirmClickListener(sDialog -> {

                    logoutDialog.cancel();

                    parseModule.logout(MainActivity.this);


                });

        logoutDialog.show();
    }


    public ToolbarFragmentDrawerCallback getMainDrawerCallback() {
        return mainDrawerCallback;
    }

}
