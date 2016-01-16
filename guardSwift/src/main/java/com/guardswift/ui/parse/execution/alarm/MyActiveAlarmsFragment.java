//package com.guardswift.ui.fragments.task.alarm;
//
//import com.guardswift.persitence.cache.GuardCache;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.parse.ParseQuery;
//
//import javax.inject.Inject;
//
//public class MyActiveAlarmsFragment extends AbstractAlarmsFragment {
//
//	protected static final String TAG = MyActiveAlarmsFragment.class.getSimpleName();
//
//	@Inject
//	GuardCache guardCache;
//
//	public static MyActiveAlarmsFragment newInstance() {
//		MyActiveAlarmsFragment fragment = new MyActiveAlarmsFragment();
//		return fragment;
//	}
//
//	public MyActiveAlarmsFragment() {
//	}
//
//
//	@Override
//	public ParseQuery<Alarm> getQueryNetwork() {
//		Guard guard = guardCache.getLoggedIn();
//		return new Alarm().getQueryBuilder(false).whereNotEnded().matching(guard).sortedByCreateDate()
//				.build();
//	}
//
//    @Override
//    public void doneLoadingObjects() {
////        geofencingModule.rebuildGeofences(new Alarm(), TAG);
//    }
//
//}
