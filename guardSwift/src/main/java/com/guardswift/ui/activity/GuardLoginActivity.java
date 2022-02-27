package com.guardswift.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingActivityModule.ForActivity;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.eventbus.events.MissingInternetEvent;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.Installation;
import com.guardswift.persistence.parse.misc.Update;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.drawer.GuardLoginNavigationDrawer;
import com.guardswift.util.Analytics;
import com.guardswift.util.Device;
import com.guardswift.util.ToastHelper;
import com.guardswift.util.UpdateApp;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import bolts.Continuation;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@RuntimePermissions
public class GuardLoginActivity extends InjectingAppCompatActivity {

    private static final String TAG = GuardLoginActivity.class.getSimpleName();

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Inject
    @ForActivity
    Context context;
    @Inject
    Device device;
    @Inject
    EventBus eventBus;
    @Inject
    ParseModule parseModule;
    @Inject
    TaskGroupStartedCache taskGroupStartedCache;
    @Inject
    Analytics mAnalytics;

    @Inject
    GuardLoginNavigationDrawer navigationDrawer;


    @BindView(R.id.imageView)
    ImageView bannerImageView;


    @BindView(R.id.login_form)
    View mLoginFormView;
    @BindView(R.id.guardid)
    EditText mGuardIdView;
    @BindView(R.id.sign_in_button)
    BootstrapButton mSignInButton;

    @BindView(R.id.login_status)
    View mLoginStatusView;
    @BindView(R.id.login_status_message)
    TextView mLoginStatusMessageView;

    @BindView(R.id.development_badge)
    View development_badge;
    @BindView(R.id.version)
    TextView version;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void start(String message) {
        Log.w(TAG, "START");
        Context context = GuardSwiftApplication.getInstance();
        Intent intent = new Intent(context, GuardLoginActivity.class);
        if (message != null) {
            intent.putExtra("MESSAGE", message);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_guard_login);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        navigationDrawer.create(this, toolbar, R.id.content);


        version.setText(device.getVersionName());
        loadBanner();

        mGuardIdView.requestFocus();
        if (!BuildConfig.DEBUG) {
            development_badge.setVisibility(View.INVISIBLE);
        }

        if (getIntent().hasExtra("MESSAGE")) {
            new CommonDialogsBuilder.MaterialDialogs(this).ok(R.string.info, getIntent().getStringExtra("MESSAGE")).show();
        }

        if (savedInstanceState == null) {
            hasGooglePlayServices();
            ignoreBatteryOptimizations();
            mAnalytics.setUserProperty(Analytics.UserProperty.COMPANY_NAME, ParseUser.getCurrentUser().getUsername());
        }

        //throw new RuntimeException("Test crashlytics!!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.updateDialog = null;

    }

    @SuppressLint("BatteryLife")
    private void ignoreBatteryOptimizations() {
        Intent intent = new Intent();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }
    }

    private void updateToolbarTitle() {
        com.parse.ParseInstallation installation = com.parse.ParseInstallation.getCurrentInstallation();

        String deviceName = installation.getString(Installation.NAME);
        String smsTo = installation.getString(Installation.MOBILE_NUMBER);

        if (deviceName != null && !deviceName.isEmpty()) {
            toolbar.setTitle(deviceName);
        }

        if (smsTo != null && !smsTo.isEmpty()) {
            toolbar.setSubtitle(smsTo);
        }
    }

    private void loadBanner() {
        String logo = ParseUser.getCurrentUser().getString("logoUrl");
        Picasso.get().load(logo).into(bannerImageView);
    }

    @OnClick(R.id.sign_in_button)
    public void login(BootstrapButton button) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            GuardLoginActivityPermissionsDispatcher.loginAndroidEqualToAndAboveQWithPermissionCheck(this);
        } else {
            GuardLoginActivityPermissionsDispatcher.loginAndroidBelowQWithPermissionCheck(this);
        }
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(mgr).hideSoftInputFromWindow(windowToken, 0);
    }


    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACTIVITY_RECOGNITION})
    public void loginAndroidEqualToAndAboveQ() {
        login();
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void loginAndroidBelowQ() {
        login();
    }

    private void login() {
        if (!device.isOnline()) {
            eventBus.post(new MissingInternetEvent());
            return;
        }

        // Reset errors.
        mGuardIdView.setError(null);


        // Store values at the time of the login attempt.
        try {
            int guardId = Integer.parseInt(mGuardIdView.getText().toString());

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            loginGuard(guardId);
        } catch (NumberFormatException e) {
            mGuardIdView.setError(getString(R.string.error_invalid_guardid));
            mGuardIdView.requestFocus();
        }
    }

    private boolean handleFailedLogin(String context, Exception e) {

        showProgress(false);
        ToastHelper.toast(GuardLoginActivity.this, context);

        if (e == null) {
            return false;
        }


        eventBus.post(new MissingInternetEvent());
        new HandleException(GuardLoginActivity.this, TAG, context, e);


        return true;
    }


    /**
     * Update Guards on Local Datastore and tries to locate Guard
     *
     * @param guardId
     */
    public void loginGuard(final int guardId) {
        Log.d(TAG, "fetching guards");

        closeKeyboard(this, mGuardIdView.getWindowToken());

        new Guard().updateAll(new ExtendedParseObject.DataStoreCallback<Guard>() {

            @Override
            public void success(List<Guard> guards) {

                Log.d(TAG, "loginGuard " + guardId);

                for (final Guard guard : guards) {

                    Log.d(TAG, "guard " + guard.getGuardId());

                    if (guard.getGuardId() == guardId) {

                        loginSuccess(guard);
                        return;
                    }
                }

                handleFailedLogin(context
                        .getString(R.string.error_guard_not_found), null);
            }

            @Override
            public void failed(ParseException e) {
                handleFailedLogin("findFrom guards", e);
            }
        });

    }

    private void loginSuccess(final Guard guard) {
        GuardSwiftApplication.getInstance().bootstrapParseObjectsLocally(GuardLoginActivity.this, guard)
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        handleFailedLogin("updateAllClasses", task.getError());
                    }
                    return task;
                })
                .onSuccess((Continuation<Void, Void>) task -> {

                    mGuardIdView.setText("");

                    parseModule.login(guard);

                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(Intent.makeRestartActivityTask(intent.getComponent()));

                    GuardLoginActivity.this.finish();

                    return null;
                });

    }




    private boolean hasGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        super.onBackPressed();
    }


    private void checkForNewUpdates() {
        Log.i(TAG, "checkForNewUpdates");

        if (installInProgress) {
            return;
        }

        new Update.QueryBuilder(false).build().addDescendingOrder(Update.versionNumber).getFirstInBackground()
                .continueWith(task -> {
                    if (task.isFaulted()) {
                        new HandleException(GuardLoginActivity.this, TAG, "Fetch updates", task.getError());
                        return null;
                    }

                    Update update = task.getResult();

                    if (update.isNewerThanInstalled()) {
                        showDownloadOption(update);
                    }

                    return null;
                });
    }


    private MaterialDialog updateDialog;
    private String updateVersionName = "";

    private void showDownloadOption(final Update targetUpdate) {
        Log.i(TAG, "showDownloadOption");

        if (isFinishing()) {
            return;
        }


        runOnUiThread(() -> {

            if (!targetUpdate.getVersionName().equals(updateVersionName)) {
                if (updateDialog != null) {
                    updateDialog.dismiss();
                }

                updateVersionName = targetUpdate.getVersionName();
                updateDialog = new MaterialDialog.Builder(GuardLoginActivity.this)
                        .title(R.string.new_update_available)
                        .positiveText(R.string.install_update)
                        .negativeText(R.string.later)
                        .content(getString(R.string.current_and_latest_version, device.getVersionName(), targetUpdate.getVersionName()))
                        .onPositive((dialog, which) -> GuardLoginActivityPermissionsDispatcher.downloadAndInstallWithPermissionCheck(GuardLoginActivity.this, targetUpdate)).build();
            }

            if (!updateDialog.isShowing()) {
                updateDialog.show();
            }

        });
    }

    private boolean installInProgress;

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void downloadAndInstall(Update update) {
        Log.i(TAG, "downloadAndInstall");

        ParseFile apkFile = update.getUpdateFile();
        if (apkFile == null) {
            ToastHelper.toast(this, getString(R.string.file_not_found));
            return;
        }

        installInProgress = true;

        final MaterialDialog downloadDialog = new MaterialDialog.Builder(this)
                .title(R.string.downloading_update)
                .content(R.string.please_wait)
                .progress(false, 100, false)
                .show();


        Log.i(TAG, "downloading update");
        apkFile.getFileInBackground((file, e) -> {

            Log.i(TAG, "downloading update done: " + (e == null));

            if (e != null) {
                new HandleException(TAG, "Failed to download update", e);
                return;
            }

            UpdateApp.fromFile(GuardLoginActivity.this, file);

            downloadDialog.dismiss();
            installInProgress = false;
        }, percentDone -> {
            Log.i(TAG, "downloading update: " + percentDone);

            downloadDialog.setProgress(percentDone);
        });
    }


    @Override
    protected void onPostResume() {

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        if (!device.hasGpsAndNetworkEnabled()) {
            showMissingLocationsDialog();
        } else {
            checkForNewUpdates();
        }

        updateToolbarTitle();

        showProgress(false);


        super.onPostResume();
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        if (mLoginFormView == null || mLoginStatusView == null) {
            return;
        }

        runOnUiThread(() -> {
            Log.d(TAG, "showLogin");
            mLoginFormView.setVisibility(show ? GONE : VISIBLE);
            mLoginStatusView.setVisibility(show ? VISIBLE : GONE);
        });
    }


    private MaterialDialog missingLocationsDialog;

    private void showMissingLocationsDialog() {

        if (missingLocationsDialog != null)
            return;

        missingLocationsDialog = new MaterialDialog.Builder(GuardLoginActivity.this)
                .title(R.string.gps_settings)
                .positiveText(R.string.open_settings)
                .content(R.string.gps_network_location_unavailable)
                .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  Intent intent = new Intent(
                                          Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                  GuardLoginActivity.this.startActivity(intent);

                                  missingLocationsDialog = null;
                              }
                          }
                ).show();
    }

    /**
     * Permissions handling
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        GuardLoginActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForPermissions(final PermissionRequest request) {
        new CommonDialogsBuilder.MaterialDialogs(this).okCancel(R.string.permissions, getString(R.string.permissions_rationale),
                (dialog, which) -> request.proceed(), (dialog, which) -> request.cancel()
        ).show();

    }


}
