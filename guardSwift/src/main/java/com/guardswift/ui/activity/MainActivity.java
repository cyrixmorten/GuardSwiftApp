package com.guardswift.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;

import com.guardswift.R;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.execution.alarm.AlarmsViewPagerFragment;
import com.guardswift.util.Device;
import com.guardswift.util.Sounds;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends InjectingAppCompatActivity implements MainNavigationDrawer.MainNavigationDrawerCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_FRAGMENT_CONTENT = "TAG_FRAGMENT_CONTENT";

    public static final String SELECT_ALARMS = "SELECT_ALARMS";


    @Inject
    GuardCache guardCache;
    @Inject
    CircuitStartedCache circuitStartedCache;
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

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Drawer messagesDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gs_activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        if (!shouldRedirectToOtherActivity()) {

            // bootstrap parseObjects if it has not been done during this session

//            if (!BuildConfig.DEBUG) {
                GuardSwiftApplication.getInstance().bootstrapParseObjectsLocally(this, guardCache.getLoggedIn());
//            } else {
//                GuardSwiftApplication.getInstance().startServices();
//            }

            setSelectionFromIntent();


            Drawer drawer = navigationDrawer.initNavigationDrawer(this, toolbar, this);

            messagesDrawer = new DrawerBuilder()
                    .withActivity(this)
                    .withDrawerGravity(Gravity.END)
                    .withCloseOnClick(false)
                    .append(drawer);

        }

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
            selectItem(AlarmsViewPagerFragment.newInstance(), R.string.alarms);
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
    }

    private void setActionBarTitle(final String title, final String subtitle) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(title);
                toolbar.setSubtitle(subtitle);
            }
        });
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

        super.onPostResume();
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        messagesDrawer = null;
        navigationDrawer = null;

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


    @Override
    public void selectItem(Fragment fragment) {
        fm.beginTransaction().replace(R.id.content, fragment).commit();
    }

    @Override
    public void selectItem(Fragment fragment, String title) {
        setActionBarTitle(title, "");
        replaceFragment(fragment);
    }

    @Override
    public void selectItem(Fragment fragment, String title, String subtitle) {
        setActionBarTitle(title, subtitle);
        replaceFragment(fragment);
    }

    @Override
    public void selectItem(Fragment fragment, int titleResource) {
        setActionBarTitle(getString(titleResource), "");
        replaceFragment(fragment);
    }

    @Override
    public void logout() {
        showLogoutDialog();
    }

    // delay a bit to allow navigation drawer to close before loading
    private void replaceFragment(final Fragment fragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!MainActivity.this.isFinishing()) {
                    // commitAllowingStateLoss is a bit brutal
                    // but as state loss errors are happening very rarely plus we are not
                    // storing state on any of the fragments it is assumed
                    // to be ok to do here.
                    fm.beginTransaction().replace(R.id.content, fragment).commitNowAllowingStateLoss();
                }
            }
        }, 500);
    }

    public Drawer getDrawer() {
        return navigationDrawer.getDrawer();
    }

    public Drawer getMessagesDrawer() {
        return messagesDrawer;
    }
}
