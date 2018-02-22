package com.guardswift.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;

import com.guardswift.R;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.eventbus.events.BootstrapCompleted;
import com.guardswift.jobs.periodic.TrackerUploadJob;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.drawer.MainNavigationDrawer;
import com.guardswift.ui.drawer.ToolbarFragmentDrawerCallback;
import com.guardswift.ui.parse.execution.alarm.AlarmsViewPagerFragment;
import com.guardswift.util.Device;
import com.guardswift.util.Sounds;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

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

    private Drawer messagesDrawer;
    private ToolbarFragmentDrawerCallback mainDrawerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gs_activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        if (!shouldRedirectToOtherActivity()) {
            GuardSwiftApplication.getInstance().bootstrapParseObjectsLocally(this, guardCache.getLoggedIn());
            initDrawer();
            setSelectionFromIntent();
        }

    }

    public void onEventMainThread(BootstrapCompleted ev) {
        initDrawer();
    }

    public void initDrawer() {
        mainDrawerCallback = new ToolbarFragmentDrawerCallback(this, toolbar, R.id.content);
        mainDrawerCallback.setActionCallback(new ToolbarFragmentDrawerCallback.SelectActionCallback() {
            public void selectAction(long action) {
                if (action == MainNavigationDrawer.DRAWER_LOGOUT) {
                    showLogoutDialog();
                }
            }
        });

        Drawer drawer = navigationDrawer.initNavigationDrawer(this, toolbar, mainDrawerCallback);

        messagesDrawer = new DrawerBuilder()
                .withActivity(this)
                .withDrawerGravity(Gravity.END)
                .withCloseOnClick(false)
                .append(drawer);
    }




    @Override
    public void onBackPressed() {
        if (messagesDrawer.isDrawerOpen()) {
            messagesDrawer.closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    private void setSelectionFromIntent() {
        if (getIntent().hasExtra(SELECT_ALARMS)) {
            mainDrawerCallback.selectItem(AlarmsViewPagerFragment.newInstance(), R.string.alarms);
            navigationDrawer.getDrawer().closeDrawer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 4.5.0: Prevent alarm sound from continuously playing
        // Can happen when multiple alarms overlap
        if (sounds.isPlayingAlarmSound()) {
            sounds.stopAlarm();
        }

//        ParseLiveQueryClient liveQueryClient = GuardSwiftApplication.getInstance().getLiveQueryClient();
//        if (liveQueryClient != null) {
//            liveQueryClient.connectIfNeeded();
//        }
    }




    // http://stackoverflow.com/questions/10216937/how-do-i-create-a-help-overlay-like-you-see-in-a-few-android-apps-and-ics
    // public void onCoachMark(){
    //
    // final Dialog dialog = new Dialog(this);
    // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    // dialog.getWindow().setBackgroundDrawable(new
    // ColorDrawable(android.graphics.Color.TRANSPARENT));
    // dialog.setContentView(R.layout.coach_mark);
    // dialog.setCanceledOnTouchOutside(true);
    // //for dismissing anywhere you touch
    // View masterView = dialog.findViewById(R.id.coach_mark_master_view);
    // masterView.setOnClickListener(new View.OnClickListener() {
    // @Override
    // public void onClick(View view) {
    // dialog.dismiss();
    // }
    // });
    // dialog.show();
    // }

    // preventing multiple calls of startActivity in redirectToOtherActivityIntent
    public boolean shouldRedirectToOtherActivity() {
        return redirectToOtherActivityIntent() != null;
    }


    public Intent redirectToOtherActivityIntent() {
        Intent intent = null;
//        if (ParseUser.getCurrentUser() == null) {
//            Log.e(TAG, "Missing user - ParseLoginActivity");
//            ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
//            intent = builder.build();
//            startActivityForResult(builder.build(), 0);
//            intent = new Intent(MainActivity.this,
//                    ParseLoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        } else
        if (!guardCache.isLoggedIn()) {
            Log.e(TAG, "Missing guard - GuardLoginActivity");
            intent = new Intent(MainActivity.this,
                    GuardLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }


    @Override
    protected void onPostResume() {
        Log.e(TAG, "onPostResume");
        if (shouldRedirectToOtherActivity()) {
            startActivity(redirectToOtherActivityIntent());
        }

        EventBusController.postUIUpdate();

        super.onPostResume();
    }



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        messagesDrawer = null;
        navigationDrawer = null;
        mainDrawerCallback = null;

        try {
            super.onDestroy();
        } catch (NullPointerException npe) {
            // https://code.google.com/p/android/issues/detail?id=216157
            Log.e(TAG, "NPE: Bug workaround");
        }

    }


    private SweetAlertDialog logoutDialog;

    private void showLogoutDialog() {
        logoutDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.logout))
                .setContentText(getString(R.string.logout_confirm))
                .setConfirmText(getString(android.R.string.yes))
                .setCancelText(getString(android.R.string.no))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {

                        logoutDialog.cancel();

                        parseModule.logout(MainActivity.this);


                    }
                });

        logoutDialog.show();
    }



    public Drawer getDrawer() {
        return navigationDrawer.getDrawer();
    }

    public MainNavigationDrawer getNavigationDrawer() {
        return navigationDrawer;
    }

    public Drawer getMessagesDrawer() {
        return messagesDrawer;
    }
}
