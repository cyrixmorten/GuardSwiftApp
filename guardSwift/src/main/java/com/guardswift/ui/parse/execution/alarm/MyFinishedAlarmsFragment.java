//package com.guardswift.ui.fragments.task.alarm;
//
//import com.guardswift.persitence.cache.GuardCache;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.parse.ParseQuery;
//
//import javax.inject.Inject;
//
//public class MyFinishedAlarmsFragment extends AbstractAlarmsFragment {
//
//	protected static final String TAG = MyFinishedAlarmsFragment.class
//			.getSimpleName();
//
//	@Inject
//	GuardCache guardCache;
//
//	public static MyFinishedAlarmsFragment newInstance() {
//		MyFinishedAlarmsFragment fragment = new MyFinishedAlarmsFragment();
//		return fragment;
//	}
//
//	public MyFinishedAlarmsFragment() {
//	}
//
//
//	@Override
//	public ParseQuery<Alarm> getQueryNetwork() {
//		Guard guard = guardCache.getLoggedIn();
//		return new Alarm().getQueryBuilder(false).finished().matching(guard).sortedByCreateDate()
//				.build();
//	}
//
//    @Override
//    public void doneLoadingObjects() {
//
//    }
//
//}
