//package com.guardswift.ui.parse.execution.circuit;
//
//import android.content.Context;
//import android.content.Intent;
//import android.support.v4.app.Fragment;
//
//import com.guardswift.R;
//import com.guardswift.persistence.cache.ParseCacheFactory;
//import com.guardswift.persistence.cache.task.CircuitUnitCache;
//import com.guardswift.persistence.parse.execution.ParseTask;
//import com.guardswift.ui.GuardSwiftApplication;
//import com.guardswift.ui.activity.AbstractToolbarActivity;
//
//import javax.inject.Inject;
//
//public class TaskDescriptionActivity extends AbstractToolbarActivity {
//
//    public static void start(Context context, ParseTask ParseTask) {
//
//        ParseCacheFactory cacheFactory = GuardSwiftApplication.getInstance().getCacheFactory();
//        cacheFactory.getTasksCache().setSelected(ParseTask);
//
//        context.startActivity(new Intent(context, TaskDescriptionActivity.class));
//    }
//
//    @Inject
//    CircuitUnitCache circuitUnitCache;
//
//    @Override
//    protected Fragment getFragment() {
//        return CircuitUnitDescriptionWebViewFragment.newInstance(circuitUnitCache.getSelected());
//    }
//
//    @Override
//    protected String getToolbarTitle() {
//        return getString(R.string.task_description);
//    }
//
//    @Override
//    protected String getToolbarSubTitle() {
//        return circuitUnitCache.getSelected().getClient().getFullAddress();
//    }
//
//}
