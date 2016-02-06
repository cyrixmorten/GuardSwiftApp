//package com.guardswift.ui.fragments.task.alarm;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentStatePagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.ActionBar;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingFragment;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class MyAlarmsViewPagerFragment extends InjectingFragment {
//
//	protected static final String TAG = MyAlarmsViewPagerFragment.class
//			.getSimpleName();
//
//	public static MyAlarmsViewPagerFragment newInstance() {
//		MyAlarmsViewPagerFragment fragment = new MyAlarmsViewPagerFragment();
//		Bundle args = new Bundle();
//		fragment.setArguments(args);
//		return fragment;
//	}
//
//	public MyAlarmsViewPagerFragment() {
//	}
//	@Inject
//	ActionBar actionBar;
//
//	@Bind(R.id.pager) public ViewPager mViewPager;
//
//	/**
//	 * The pager adapter, which provides the pages to the view pager widget.
//	 */
//	public AlarmsPagerAdapter mPagerAdapter;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		setHasOptionsMenu(true);
//		setRetainInstance(true);
//
//		mPagerAdapter = new AlarmsPagerAdapter(getChildFragmentManager());
//		super.onCreate(savedInstanceState);
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		View rootView = inflater.inflate(R.layout.viewpager_titlestrip,
//				container, false);
//
//		ButterKnife.bind(this, rootView);
//
//		mViewPager.setAdapter(mPagerAdapter);
//		mViewPager.setOffscreenPageLimit(AlarmsPagerAdapter.FRAGMENTS_COUNT);
//
//		// restoreActionBar();
//
//		return rootView;
//	}
//
//	@Override
//	public void onDestroy() {
//		ButterKnife.unbind(this);
//		super.onDestroy();
//	}
//
//	@Deprecated
//	public void restoreActionBar() {
//		actionBar.setTitle(getString(R.string.title_alarms));
//		actionBar.setSubtitle(null);
//	}
//
//	private class AlarmsPagerAdapter extends FragmentStatePagerAdapter {
//
//		public static final int FRAGMENTS_COUNT = 2;
//
//		public int POSITION_NEW = 0;
//		public int POSITION_CLOSED = 1;
//
//		public AlarmsPagerAdapter(FragmentManager fm) {
//			super(fm);
//		}
//
//		@Override
//		public Fragment getItem(int clientPosition) {
//			Log.d(TAG, "getItem: " + clientPosition);
//			if (clientPosition == POSITION_NEW) {
//				return MyActiveAlarmsFragment.newInstance();
//			}
//			if (clientPosition == POSITION_CLOSED) {
//				return MyFinishedAlarmsFragment.newInstance();
//			}
//			return null;
//		}
//
//		@Override
//		public CharSequence getPageTitle(int clientPosition) {
//			Log.d(TAG, "getItem: " + clientPosition);
//			if (clientPosition == POSITION_NEW) {
//				return getActivity().getString(R.string.title_alarms_new);
//			}
//			if (clientPosition == POSITION_CLOSED) {
//				return getActivity().getString(R.string.title_alarms_old);
//			}
//
//			return super.getPageTitle(clientPosition);
//		}
//
//		@Override
//		public int getCount() {
//			return FRAGMENTS_COUNT;
//		}
//	}
//
//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//	}
//
//	@Override
//	public void onDetach() {
//		super.onDetach();
//	}
//
//}
