package com.guardswift.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingActivityModule.ForActivity;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.eventbus.events.MissingInternetEvent;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseObjectFactory;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.rest.GuardSwiftWeb;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Device;
import com.guardswift.util.GSIntents;
import com.guardswift.util.ToastHelper;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class GuardLoginActivity extends InjectingAppCompatActivity {

    private static final String TAG = GuardLoginActivity.class.getSimpleName();

    public static final String UPDATE_VERSIONCHECK_URL = "http://www.guardswift.com/downloads/latest-versioncode.txt";
    public static final String UPDATE_DOWNLOAD_URL = "http://www.guardswift.com/downloads/guardswift.apk";

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Inject
    @ForActivity
    Context context;
    @Inject
    Device device;
    @Inject
    ParseObjectFactory parseObjectFactory;
    //	@Inject LocationsModule locationModule;
    @Inject
    EventBus eventBus;
    @Inject
    ParseModule parseModule;
    @Inject
    CircuitStartedCache circuitsStartedCache;

    // UI references.
//    @Bind(R.id.switch_receive_alarms)
//    Switch mReceiveAlarms;
//    @Bind(R.id.layout_alarm_group)
//    RelativeLayout mLayoutAlarmGroup;
//    @Bind(R.id.spinner_signed_alarm_group)
//    Spinner mAlarmGroupSigned;
//    @Bind(R.id.spinner_responsible_alarm_group)
//    Spinner mAlarmGroupResponsible;


    @Bind(R.id.imageView)
    ImageView bannerImageView;


    @Bind(R.id.login_form)
    View mLoginFormView;
    @Bind(R.id.guardid)
    EditText mGuardIdView;
    @Bind(R.id.sign_in_button)
    BootstrapButton mSignInButton;

    @Bind(R.id.login_status)
    View mLoginStatusView;
    @Bind(R.id.login_status_message)
    TextView mLoginStatusMessageView;

    @Bind(R.id.development_badge)
    View development_badge;
    @Bind(R.id.version)
    TextView version;


//    private DownloadUpdate mDownloadTask;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void start(Context context) {
        Log.w(TAG, "START");
        Intent intent = new Intent(context, GuardLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_guard_login);
        ButterKnife.bind(this);


        hasGooglePlayServices();
//        registerUpdateDownloadedReceiver();

        version.setText(device.getVersionName());
        loadBanner();

        mGuardIdView.requestFocus();
        if (!BuildConfig.DEBUG) {
            development_badge.setVisibility(View.INVISIBLE);
        }

    }
//
//    public MaterialDialog updateDownloadingProgress;
//    private long enqueue;
//    private DownloadManager dm;
//    private BroadcastReceiver updateDownloadReceiver;
//
//    private void registerUpdateDownloadedReceiver() {
//        updateDownloadReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
////                    long downloadId = intent.getLongExtra(
////                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(enqueue);
//                    Cursor c = dm.query(query);
//                    if (c.moveToFirst()) {
//                        Log.d(TAG, "DownloadMsg: " + statusMessage(c));
//                        int columnIndex = c
//                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
//                        if (DownloadManager.STATUS_SUCCESSFUL == c
//                                .getInt(columnIndex)) {
//
//                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
//                            Log.d(TAG, "Downloaded: " + uriString);
//
//                            launchInstaller(new File(uriString));
//
//                            if (updateDownloadingProgress != null) {
//                                updateDownloadingProgress.cancel();
//                                updateDownloadingProgress = null;
//                            }
//
//                        }
//                    }
//                }
//            }
//        };
//
//        registerReceiver(updateDownloadReceiver, new IntentFilter(
//                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//    }
//
//    private String statusMessage(Cursor c) {
//        String msg = "???";
//
//        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
//            case DownloadManager.STATUS_FAILED:
//                msg = "Download failed!";
//                break;
//
//            case DownloadManager.STATUS_PAUSED:
//                msg = "Download paused!";
//                break;
//
//            case DownloadManager.STATUS_PENDING:
//                msg = "Download pending!";
//                break;
//
//            case DownloadManager.STATUS_RUNNING:
//                msg = "Download in progress!";
//                break;
//
//            case DownloadManager.STATUS_SUCCESSFUL:
//                msg = "Download complete!";
//                break;
//
//            default:
//                msg = "Download is nowhere in sight";
//                break;
//        }
//
//        return (msg);
//    }

    private void loadBanner() {
        String logo = ParseUser.getCurrentUser().getString("logoUrl");
        Picasso.with(context).load(logo).into(bannerImageView);
    }

    @OnClick(R.id.sign_in_button)
    public void login(BootstrapButton button) {
//        parseModule.clearData();
        attemptLogin();
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    public void attemptLogin() {

        Log.d(TAG, "attemptLogin");

        if (!device.isOnline()) {
            eventBus.post(new MissingInternetEvent());
            return;
        }

        // Reset errors.
        mGuardIdView.setError(null);

        boolean cancel = false;
        View focusView = null;

        int mGuardId = 0;
        // Store values at the time of the login attempt.
        try {
            mGuardId = Integer.parseInt(mGuardIdView.getText().toString());
        } catch (NumberFormatException e) {
            mGuardIdView.setError(getString(R.string.error_invalid_guardid));
            focusView = mGuardIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            loginGuard(mGuardId);
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
     * Updates Guards on Local Datastore and tries to locate Guard
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
                        GuardSwiftApplication.getInstance().bootstrapParseObjectsLocally(GuardLoginActivity.this, guard).continueWith(new Continuation<Void, Void>() {

                            @Override
                            public Void then(Task<Void> result) throws Exception {
                                if (result.isFaulted()) {
                                    handleFailedLogin("updateAllClasses", result.getError());
                                    return null;
                                }

                                showProgress(false);
                                mGuardIdView.setText("");
                                parseModule.login(guard);

                                return null;
                            }
                        });

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


    private boolean hasGooglePlayServices() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (result != ConnectionResult.SUCCESS) {
            Crashlytics.log("GooglePlayServices: " + result);
            GooglePlayServicesUtil.showErrorDialogFragment(result, this, 10);
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
//        if (Guard.Recent.getSelected() != null) {
//            showProgress(false);
//        } else {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
//        }
    }

//    public void onEventMainThread(DataSyncEvent ev) {
//        String message = getString(R.string.login_progress_synchronizing,
//                ev.missingUpdates, ev.totalUpdates);
//        mLoginStatusMessageView.setText(message);
//    }

    @Override
    protected void onResume() {

        checkForNewUpdate(new VersionCheckCallback() {
            @Override
            public void newVersionAvailable(boolean newUpdate) {
                if (newUpdate) {
                    showDownloadOption();
                }
            }
        });


//        new FetchLatestVersion(this).execute();

        super.onResume();
    }

    private interface VersionCheckCallback {
        void newVersionAvailable(boolean result);
    }

    private void checkForNewUpdate(final VersionCheckCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GuardSwiftWeb.API_URL)
                .build();

        GuardSwiftWeb.API guardSwift = retrofit.create(GuardSwiftWeb.API.class);

        guardSwift.version().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String latestversion = response.body().string();

                        boolean higherVersion = Integer.parseInt(latestversion) > device.getVersionCode();

                        callback.newVersionAvailable(higherVersion);

                    } catch (IOException e) {
                        new HandleException(TAG, "Version check response", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                new HandleException(TAG, "Error at version check", t);

                callback.newVersionAvailable(false);
            }
        });
    }


    @Override
    protected void onPostResume() {

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        if (!device.hasGpsAndNetworkEnabled()) {
            showMissingLocationsDialog();
        }

        super.onPostResume();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        unregisterReceiver(updateDownloadReceiver);
//        if (mDownloadTask != null) {
//            MaterialDialog dialog = mDownloadTask.working_dialog;
//            if (dialog != null) {
//                dialog.cancel();
//            }
//        }
        super.onDestroy();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (mLoginFormView == null || mLoginStatusView == null)
            return;

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    private class FetchLatestVersion extends AsyncTask<Void, Void, Integer> {
//
//        private WeakReference<Activity> mActRef;
//
//        public FetchLatestVersion(Activity activity) {
//            mActRef = new WeakReference<Activity>(activity);
//        }
//
//        @Override
//        protected Integer doInBackground(Void... param) {
//
//            String inputLine = "-1";
//            BufferedReader in = null;
//            try {
//                URL update_url = new URL(UPDATE_VERSIONCHECK_URL);
//                in = new BufferedReader(new InputStreamReader(
//                        update_url.openStream()));
//
//                if ((inputLine = in.readLine()) != null) {
//                    return Integer.parseInt(inputLine);
//                }
//
//            } catch (Exception e) {
//                new HandleException(TAG, "Version check" + e.getMessage(), e);
//            } finally {
//                try {
//                    if (in != null) {
//                        in.close();
//                    }
//                } catch (IOException e) {
//                    new HandleException(TAG, "Version check: " + e.getMessage(), e);
//                }
//            }
//            return -1;
//
//        }
//
//        @Override
//        protected void onPostExecute(Integer result) {
//            if (result > device.getVersionCode()) {
//                showDownloadOption();
//            }
//
//            Log.d(TAG, "versioncheck complete. latest: " + result + " current: " + device.getVersionCode());
//        }
//
//
//    }

    private void showDownloadOption() {

//            findViewById(R.id.new_update).setVisibility(View.VISIBLE);

        if (isFinishing())
            return;

        new MaterialDialog.Builder(GuardLoginActivity.this)
                .title(R.string.update)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .content(R.string.new_update_available)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        GSIntents.openGmail(GuardLoginActivity.this);
                    }
                })
                .show();
    }


//    //    @OnClick(R.id.button_download)
//    public void update(Button button) {
//        if (mDownloadTask != null
//                && mDownloadTask.getStatus() == Status.RUNNING) {
//            mDownloadTask.cancel(true);
//        }
//
//        mDownloadTask = new DownloadUpdate();
//        mDownloadTask.execute();
//
////        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
////        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(UPDATE_DOWNLOAD_URL));
////        enqueue = dm.enqueue(request);
//
////        showDownloadProgress();
////        monitorDownloadProgress();
//    }


//    private void showDownloadProgress() {
//        updateDownloadingProgress = new MaterialDialog.Builder(GuardLoginActivity.this)
//                .title(R.string.downloading_update)
//                .content(R.string.please_wait)
//                .progress(false, 0)
//                .cancelable(false)
//                .negativeText(android.R.string.cancel)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onNegative(MaterialDialog dialog) {
//                        dm.remove(enqueue);
//                        Toast.makeText(getApplicationContext(), getString(R.string.download_canceled), Toast.LENGTH_LONG).show();
//                    }
//                })
//                .show();
//    }

//    private void monitorDownloadProgress() {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//
//                boolean downloading = true;
//
//                while (downloading) {
//
//                    DownloadManager.Query q = new DownloadManager.Query();
//                    q.setFilterById(enqueue);
//
//                    Cursor cursor = dm.query(q);
//                    cursor.moveToFirst();
//                    int bytes_downloaded = cursor.getInt(cursor
//                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//
//                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
//
//                        String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
//                        launchInstaller(new File(uriString));
//
//                        if (updateDownloadingProgress != null) {
//                            updateDownloadingProgress.cancel();
//                            updateDownloadingProgress = null;
//                        }
//
//                        downloading = false;
//                    }
//
//                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
//                    Log.d(TAG, "Progress: " + dl_progress);
//
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//
//                            updateDownloadingProgress.setProgress(dl_progress);
//
//                        }
//                    });
//
//                    cursor.close();
//                }
//
//            }
//        }).start();
//    }

//    private class DownloadUpdate extends AsyncTask<Void, Integer, File> {
//
//        public MaterialDialog working_dialog;
//
//        private String toastMsg = "";
//
//        @Override
//        protected void onPreExecute() {
//            working_dialog = new MaterialDialog.Builder(GuardLoginActivity.this)
//                    .title(R.string.downloading_update)
//                    .content(R.string.please_wait)
//                    .progress(true, 0)
//                    .cancelable(false)
//                    .negativeText(android.R.string.cancel)
//                    .callback(new MaterialDialog.ButtonCallback() {
//                        @Override
//                        public void onNegative(MaterialDialog dialog) {
//                            DownloadUpdate.this.cancel(true);
//                            Toast.makeText(getApplicationContext(), getString(R.string.download_canceled), Toast.LENGTH_LONG).show();
//                        }
//                    })
//                    .show();
//        }
//
//        @Override
//        protected File doInBackground(Void... param) {
//
//            String filename = "guardswift.apk";
//
//
//            File myFilesDir = new File(Environment
//                    .getExternalStorageDirectory().getAbsolutePath()
//                    + "/Download");
////            File file = new File(context.getFilesDir(), "update.apk");
//            File file = new File(myFilesDir, filename);
//
//            if (file.exists()) {
//                file.delete();
//            }
//
//            if ((myFilesDir.mkdirs() || myFilesDir.isDirectory())) {
//
//                HttpURLConnection c = null;
//
//                try {
//
//                    URL url = new URL(UPDATE_DOWNLOAD_URL);
//                    c = (HttpURLConnection) url.openConnection();
//                    c.setRequestMethod("GET");
////                    c.setDoOutput(true);
//                    c.connect();
//
//                    InputStream is = c.getInputStream();
////                    FileOutputStream fos = openFileOutput("update.apk", Context.MODE_PRIVATE);
//                    FileOutputStream fos = new FileOutputStream(myFilesDir
//                            + "/" + filename);
//
//                    byte[] buffer = new byte[1024];
//                    int len1 = 0;
//                    int total = 0;
//                    int length = c.getContentLength();
//                    while ((len1 = is.read(buffer)) != -1) {
//                        total += len1;
//                        int progress = (int) (total * 100 / length);
//                        publishProgress(progress);
//                        Log.d(TAG, "progress: " + progress + "/" + total);
//
//                        fos.write(buffer, 0, len1);
//                    }
//                    fos.close();
//                    is.close();
//                } catch (Exception e) {
//                    new HandleException(GuardLoginActivity.this, TAG, "download update", e);
//                    toastMsg = context
//                            .getString(R.string.title_internet_missing);
//                }
//            }
//
//            return file;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//            if (working_dialog != null && !working_dialog.isIndeterminateProgress()) {
//                working_dialog.setProgress(values[0]);
//            }
//        }
//
//        @Override
//        protected void onPostExecute(File file) {
//
//            if (!toastMsg.isEmpty() && context != null) {
//                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
//            } else {
//                launchInstaller(file);
//            }
//
//            removeWorkingDialog();
//        }
//
//        private void removeWorkingDialog() {
//            if (working_dialog != null && working_dialog.isShowing()) {
//                working_dialog.dismiss();
//                working_dialog = null;
//            }
//        }
//
//    }
//
//    public void launchInstaller(File apkFile) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.fromFile(apkFile),
//                "application/vnd.android.package-archive");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivityForResult(intent, 0);
//    }

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


}
