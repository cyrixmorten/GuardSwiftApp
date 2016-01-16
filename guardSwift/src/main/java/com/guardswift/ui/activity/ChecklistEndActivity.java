//package com.guardswift.ui.activity;
//
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingFragmentActivity;
//
//public class ChecklistEndActivity extends InjectingFragmentActivity {
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		setContentView(R.layout.activity_checklist_end);
//
//		ActionBar actionBar = getSupportActionBar();
//		actionBar.setDisplayShowHomeEnabled(true);
//		actionBar.setTitle(getString(R.string.title_checklist));
//		actionBar.setSubtitle(getString(R.string.checklist_header_circuit_end));
//
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case android.R.id.home:
//			cancel();
//			return true;
//
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public void onBackPressed() {
//		cancel();
//	}
//
//	private void cancel() {
//		Intent intent = new Intent(ChecklistEndActivity.this,
//				MainActivity.class);
//		startActivity(intent);
//		this.onActionFinish();
//	}
//
//}
