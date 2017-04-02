package com.guardswift.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ParseLoginActivity extends InjectingAppCompatActivity {

	private static final String TAG = ParseLoginActivity.class.getSimpleName();

	public interface ParseLoginCallback {
		void success();
		void failed(ParseException e);
	}


	private String mUsername;

	// UI references.
//	@Bind(R.id.devicename) EditText mDevicenameView;
	@Bind(R.id.username) EditText mUsernameView;
	@Bind(R.id.password) EditText mPasswordView;
	@Bind(R.id.login_form) View mLoginFormView;
	@Bind(R.id.login_status) View mLoginStatusView;
	@Bind(R.id.login_status_message) TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.gs_activity_login);

		ButterKnife.bind(this);

		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});



	}

	@OnClick(R.id.sign_in_button)
	public void login(Button button) {
		attemptLogin();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		super.onBackPressed();
	}

	public void attemptLogin() {

		Log.d(TAG, "attemptLogin");

		// Reset errors.
//		mDevicenameView.setError(null);
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
//		mDevicename = mDevicenameView.getText().toString();
		mUsername = mUsernameView.getText().toString();
		String mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid devicename.
//		if (TextUtils.isEmpty(mDevicename)) {
//			mDevicenameView.setError(getString(R.string.error_field_required));
//			focusView = mDevicenameView;
//			cancel = true;
//		}

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid usename address.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
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

			// parsePreferences.setDevicename(mDevicename);
			// parsePreferences.setUsername(mUsername);
			// parsePreferences.setPassword(mPassword);

			login(mUsername, mPassword, new ParseLoginCallback() {

				@Override
				public void success() {

					Intent intent = new Intent(ParseLoginActivity.this,
							GuardLoginActivity.class);
					intent.putExtra("initial_startup", true);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);

					// close this activity
					finish();

				}

				@Override
				public void failed(ParseException e) {
					Toast.makeText(ParseLoginActivity.this, e.getMessage(),
							Toast.LENGTH_LONG).show();

					handleFailedLogin("parseLogin", e);
				}
			});
		}
	}

	private boolean handleFailedLogin(String context, ParseException e) {

		if (e == null) {
			return false;
		}

		Log.e(TAG, context, e);

		handleFailedLogin(e.getMessage());

		return true;
	}

	private void handleFailedLogin(String message) {
		Toast.makeText(ParseLoginActivity.this, message, Toast.LENGTH_LONG)
				.show();
		showProgress(false);
	}

	public void login(String username, String password, final ParseLoginCallback parseLoginCallback) {

		ParseUser.logInInBackground(username, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {

					ParseInstallation installation = ParseInstallation
							.getCurrentInstallation();
					installation.put("owner", ParseUser.getCurrentUser());
					installation.setACL(new ParseACL(ParseUser.getCurrentUser()));

					installation.saveEventually();

					parseLoginCallback.success();

				} else {
					parseLoginCallback.failed(e);
				}
			}
		});
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
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

}
