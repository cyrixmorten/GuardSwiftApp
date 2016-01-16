//package com.guardswift.ui.activity;
//
//
//import android.os.Bundle;
//import android.support.v4.app.NavUtils;
//import android.view.MenuItem;
//
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingFragmentActivity;
//import com.guardswift.modules.parseModule.ParseModule;
//
//import javax.inject.Inject;
//
//public class ChecklistStartActivity extends InjectingFragmentActivity {
//
//	@Inject
//    ParseModule parseModule;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		setContentView(R.layout.activity_checklist_start);
//
//		ActionBar actionBar = getSupportActionBar();
//		actionBar.setDisplayShowHomeEnabled(true);
//		actionBar.setTitle(getString(R.string.title_checklist));
//		actionBar
//				.setSubtitle(getString(R.string.checklist_header_circuit_start));
//
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case android.R.id.home:
//            parseModule.logout(false);
//			NavUtils.navigateUpFromSameTask(this);
//			return true;
//
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//}
