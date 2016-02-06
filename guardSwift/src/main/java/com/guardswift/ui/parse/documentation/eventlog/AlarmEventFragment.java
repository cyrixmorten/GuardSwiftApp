//package com.guardswift.ui.parse.documentation.eventlog;
//
//import android.content.Context;
//import android.os.Bundle;
//
//import com.guardswift.persistence.cache.task.AlarmCache;
//import com.guardswift.persistence.parse.documentation.event.EventLog;
//import com.guardswift.persistence.parse.execution.GSTask;
//import com.guardswift.persistence.parse.execution.task.alarm.Alarm;
//import com.guardswift.ui.GuardSwiftApplication;
//import com.guardswift.ui.activity.GSTaskCreateReportActivity;
//import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
//import com.parse.ParseQuery;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//public class AlarmEventFragment extends AbstractEventFragment {
//
//	protected static final String TAG = AlarmEventFragment.class
//			.getSimpleName();
//
//    public static AlarmEventFragment newInstance(Context context, Alarm alarm) {
//
//        GuardSwiftApplication.getInstance().getCacheFactory().getAlarmCache().setSelected(alarm);
//
//        AlarmEventFragment fragment = new AlarmEventFragment();
//        Bundle args = new Bundle();
//        args.putBoolean(GSTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON, true);
//        args.putString(GSTaskCreateReportActivity.FILTER_EVENT, "");
//        fragment.setArguments(args);
//        return fragment;
//    }
//
////    public static AlarmEventFragment newInstance(Bundle bundle) {
////        AlarmEventFragment fragment = new AlarmEventFragment();
////        fragment.setArguments(bundle);
////        return fragment;
////    }
//
//    @Inject
//    AlarmCache alarmCache;
//
//	public AlarmEventFragment() {
//	}
//
//
//    @Override
//    ParseQuery<EventLog> getEventLogQuery(List<String> filterEvents, boolean excludePerimiterEvents, boolean excludeAutomaticEvent) {
//        EventLog.QueryBuilder builder = EventLog.getQueryBuilder(true);
//
//        if (excludeAutomaticEvent)
//            builder.excludeAutomatic();
//
////        if (excludePerimiterEvents)
////            builder.whereIsReportEntry();
//
//        builder.matchingEvents(filterEvents);
//
//        builder.matching(alarmCache.getSelected().getClient());
//
//        return builder.build();
//    }
//
//    @Override
//    void openAddEvent() {
//        CreateEventHandlerActivity.start(getContext(), alarmCache.getSelected());
//    }
//
//    @Override
//    GSTask.TASK_TYPE getFragmentType() {
//        return GSTask.TASK_TYPE.ALARM;
//    }
//
//    @Override
//    GSTask getTaskPointer() {
//        return alarmCache.getSelected();
//    }
//
//
//}
