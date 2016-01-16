//package com.guardswift.ui.fragments.task.alarm;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentStatePagerAdapter;
//import android.support.v4.view.ViewPager;
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
//public class AllAlarmsViewPagerFragment extends InjectingFragment {
//
//	protected static final String TAG = AllAlarmsViewPagerFragment.class
//			.getSimpleName();
//
//	public static AllAlarmsViewPagerFragment newInstance() {
//		AllAlarmsViewPagerFragment fragment = new AllAlarmsViewPagerFragment();
//		Bundle args = new Bundle();
//		fragment.setArguments(args);
//		return fragment;
//	}
//
//	public AllAlarmsViewPagerFragment() {
//	}
//
//	@Inject
//	android.support.v7.app.ActionBar actionBar;
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
//		public Fragment getItem(int position) {
//			Log.d(TAG, "getItem: " + position);
//			if (position == POSITION_NEW) {
//				return AllPendingAlarmsFragment.newInstance();
//			}
//			if (position == POSITION_CLOSED) {
//				return AllClosedAlarmsFragment.newInstance();
//			}
//			return null;
//		}
//
//		@Override
//		public CharSequence getPageTitle(int position) {
//			Log.d(TAG, "getItem: " + position);
//			if (position == POSITION_NEW) {
//				return getActivity().getString(R.string.title_alarms_new);
//			}
//			if (position == POSITION_CLOSED) {
//				return getActivity().getString(R.string.title_alarms_old);
//			}
//
//			return super.getPageTitle(position);
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
