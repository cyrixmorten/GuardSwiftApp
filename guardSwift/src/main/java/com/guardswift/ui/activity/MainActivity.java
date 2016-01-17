package com.guardswift.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.util.Device;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends InjectingAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_FRAGMENT_CONTENT = "TAG_FRAGMENT_CONTENT";


    @Inject
    GuardCache guardCache;
    @Inject
    CircuitStartedCache circuitStartedCache;
    @Inject
    ParseModule parseModule;
    @Inject
    Device device;
    @Inject
    FragmentManager fm;
    @Inject MainNavigationDrawer navigationDrawer;


    @Bind(R.id.toolbar)
    Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gs_activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        if (!shouldRedirectToOtherActivity()) {

            navigationDrawer.initNavigationDrawer(this, toolbar, new MainNavigationDrawer.MainNavigationDrawerCallback() {
                @Override
                public void selectItem(Fragment fragment) {
                    fm.beginTransaction().replace(R.id.container, fragment).commit();
                }

                @Override
                public void selectItem(Fragment fragment, String title) {
                    Log.w(TAG, "Item - string: " + title);
                    setActionBarTitle(title);
                    replaceFragment(fragment);
                }

                @Override
                public void selectItem(Fragment fragment, int titleResource) {
                    Log.w(TAG, "Item - int: " + titleResource);
                    setActionBarTitle(getString(titleResource));
                    replaceFragment(fragment);
                }

                @Override
                public void logout() {
                    showLogoutDialog();
                }

                private void replaceFragment(Fragment fragment) {
                    fm.beginTransaction().replace(R.id.container, fragment).commit();
                }
            });

        }
    }

    private void setActionBarTitle(final String title) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(title);
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
        if (ParseUser.getCurrentUser() == null) {
            Log.e(TAG, "Missing user - ParseLoginActivity");
            intent = new Intent(MainActivity.this,
                    ParseLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (!guardCache.isLoggedIn()) {
            Log.e(TAG, "Missing guard - GuardLoginActivity");
            intent = new Intent(MainActivity.this,
                    GuardLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }

    @Override
    public void onBackPressed() {
        // do nothing
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
        Log.e(TAG, "onDestroy");
        navigationDrawer = null;
        super.onDestroy();
    }




    private void showLogoutDialog() {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.logout))
                .setContentText(getString(R.string.logout_confirm))
                .setConfirmText(getString(android.R.string.yes))
                .setCancelText(getString(android.R.string.no))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {
                        if (!device.isOnline()) {
                            // Missing internet connection
                            logoutErrorDialog(getString(R.string.title_internet_missing), getString(R.string.could_not_connect_to_server));
                            return;
                        }
                        // Prepare progress dialog
                        final MaterialDialog updateDialog = new MaterialDialog.Builder(MainActivity.this)
                                .title(R.string.working)
                                .content(R.string.please_wait)
                                .progress(false, 0, true)
                                .show();

                        updateDialog.setMaxProgress(100);
                        updateDialog.setCancelable(false);

//                        sDialog
//                                .setTitleText(getString(R.string.working))
//                                .setContentText(getString(R.string.please_wait))
////                                    .setConfirmText(getString(android.R.string.ok))
//                                .showCancelButton(false)
//                                .setConfirmClickListener(null)
//
//                                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
//
//                        sDialog.setCancelable(false);


                        // Perform logout, upload
                        parseModule.logout(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Log.d(TAG, "Logout completed! " + e);
                                if (e != null) {
                                    if (e.getCode() == ParseException.TIMEOUT) {
                                        logoutErrorDialog(getString(R.string.title_internet_missing), getString(R.string.could_not_connect_to_server));
                                    } else {
                                        logoutErrorDialog(getString(R.string.error_an_error_occured), e.getMessage());
                                    }

                                    new HandleException(TAG, "Guard logout", e);

                                    updateDialog.dismiss();
                                    return;
                                }


                            }
                        }, new ProgressCallback() {
                            @Override
                            public void done(Integer percentDone) {
                                Log.e(TAG, "Logout progress: " + percentDone);
//                                sDialog.getProgressHelper()
//                                        .setProgress(percentDone);
                                updateDialog.setProgress(percentDone);
                            }
                        });


                    }
                })
                .show();
    }

    private void logoutErrorDialog(String title, String content) {
        new MaterialDialog.Builder(MainActivity.this)
                .title(title)
                .content(content)
                .show();

//        sDialog
//                .setTitleText(title)
//                .setContentText(content)
//                .setConfirmText(getString(android.R.string.ok))
//                .setConfirmClickListener(null)
//                .showCancelButton(false)
//                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
    }
}
