//package com.guardswift.ui.parse.documentation.report.create.activity;
//
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.PersistableBundle;
//
//import com.guardswift.persistence.cache.ParseCacheFactory;
//import com.guardswift.persistence.cache.task.CircuitUnitCache;
//import com.guardswift.persistence.parse.documentation.event.EventLog;
//import com.guardswift.persistence.parse.documentation.event.EventLog.EventCodes;
//import com.guardswift.persistence.parse.execution.ParseTask;
//import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
//import com.guardswift.ui.GuardSwiftApplication;
//
//import java.util.Date;
//
//import javax.inject.Inject;
//
//public class CircuitUnitCreateEventHandlerActivity extends AbstractCreateEventHandlerActivity {
//
//    private static final String TAG = CircuitUnitCreateEventHandlerActivity.class
//            .getSimpleName();
//
//    public static void start(Context context, CircuitUnit circuitUnit) {
//
//        ParseCacheFactory cacheFactory = GuardSwiftApplication.getInstance().getCacheFactory();
//        cacheFactory.getCircuitUnitCache().setSelected(circuitUnit);
//
//        context.startActivity(new Intent(context, CircuitUnitCreateEventHandlerActivity.class));
//    }
//
//    @Inject
//    CircuitUnitCache circuitUnitCache;
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//    }
//
//
////    @Override
////    Client getClient() {
////        return circuitUnitCache.getSelected().getClient();
////    }
//
//    @Override
//    void saveEvent(Date timestamp, String event, int amount, String people, String clientLocation,
//                   String remarks) {
//
//        new EventLog.Builder(this)
//                .taskPointer(circuitUnitCache.getSelected(), ParseTask.EVENT_TYPE.OTHER)
//                .event(event)
//                .amount(amount)
//                .people(people)
//                .location(clientLocation)
//                .remarks(remarks)
//                .deviceTimeStamp(timestamp)
//                .eventCode(EventCodes.REGULAR_OTHER).saveAsync();
//
////        mCircuitUnit.getTaskSummaryInstance().event(eventlog);
//
//    }
//
//
//}
