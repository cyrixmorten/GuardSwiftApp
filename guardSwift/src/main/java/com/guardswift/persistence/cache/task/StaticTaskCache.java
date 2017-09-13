//package com.guardswift.persistence.cache.task;
//
//import android.content.Context;
//
//import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;
//import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
///**
// * Created by cyrix on 10/21/15.
// */
//@Singleton
//public class StaticTaskCache extends BaseTaskCache<StaticTask> {
//
//    @Inject
//    StaticTaskCache(@ForApplication Context context) {
//        super(StaticTask.class, context);
//    }
//
//    @Override
//    public StaticTask getConcreteTask() {
//        return new StaticTask();
//    }
//
//
//}
