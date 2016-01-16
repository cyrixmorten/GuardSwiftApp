//package com.guardswift.ui.fragments.task.alarm;
//
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.parse.ParseQuery;
//
//public class AllClosedAlarmsFragment extends AbstractAlarmsFragment {
//
//	protected static final String TAG = AllClosedAlarmsFragment.class
//			.getSimpleName();
//
//	public static AllClosedAlarmsFragment newInstance() {
//		AllClosedAlarmsFragment fragment = new AllClosedAlarmsFragment();
//		return fragment;
//	}
//
//	public AllClosedAlarmsFragment() {
//	}
//
//	@Override
//	public ParseQuery<Alarm> getQueryNetwork() {
//		return new Alarm().getQueryBuilder(false).whereEnded().sortedByCreateDate().build();
//	}
//
//    @Override
//    public void doneLoadingObjects() {
//
//    }
//
//}
