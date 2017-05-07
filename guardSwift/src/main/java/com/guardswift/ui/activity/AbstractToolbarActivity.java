package com.guardswift.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class AbstractToolbarActivity extends InjectingAppCompatActivity {

	private static final String TAG = AbstractToolbarActivity.class.getSimpleName();


	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@BindView(R.id.content)
	RelativeLayout content;

	protected abstract Fragment getFragment();
	protected abstract String getToolbarTitle();
	protected abstract String getToolbarSubTitle();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.gs_activity_toolbar);

		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDefaultDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			if (getToolbarTitle() != null && !getToolbarTitle().isEmpty()) {
				actionBar.setTitle(getToolbarTitle());
			}
			if (getToolbarSubTitle() != null && !getToolbarSubTitle().isEmpty()) {
				actionBar.setSubtitle(getToolbarSubTitle());
			}
		}

		if (getFragment() != null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.content, getFragment()).commit();
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}


		return super.onOptionsItemSelected(item);
	}



}
