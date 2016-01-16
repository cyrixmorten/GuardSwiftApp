//package com.guardswift.ui.fragments.task.alarm;
//
//import com.guardswift.core.ca.GeofencingModule;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.parse.ParseQuery;
//
//import javax.inject.Inject;
//
//public class AllPendingAlarmsFragment extends AbstractAlarmsFragment {
//
//	protected static final String TAG = AllPendingAlarmsFragment.class
//			.getSimpleName();
//
//    @Inject
//    GeofencingModule geofencingModule;
//
//	public static AllPendingAlarmsFragment newInstance() {
//		AllPendingAlarmsFragment fragment = new AllPendingAlarmsFragment();
//		return fragment;
//	}
//
//	public AllPendingAlarmsFragment() {
//	}
//
//	@Override
//	public ParseQuery<Alarm> getQueryNetwork() {
//		return new Alarm().getQueryBuilder(false).whereNotEnded().sortedByCreateDate().build();
//	}
//
//    @Override
//    public void doneLoadingObjects() {
////        geofencingModule.rebuildGeofences(new Alarm(), TAG);
//    }
//
//}
