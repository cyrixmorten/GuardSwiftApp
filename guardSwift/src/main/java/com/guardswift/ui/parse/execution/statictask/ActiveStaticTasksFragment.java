package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.StaticTaskQueryBuilder;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ui.widget.ParseQueryAdapter;

public class ActiveStaticTasksFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = ActiveStaticTasksFragment.class.getSimpleName();


	public static ActiveStaticTasksFragment newInstance() {
		return new ActiveStaticTasksFragment();
	}

	public ActiveStaticTasksFragment() {
        this.setReloadOnResume();
	}


    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return () -> new StaticTaskQueryBuilder(false).
                status(ParseTask.STATUS.ARRIVED).
                daysBack(7).
                sortedByCreated().
                build();
    }

//    @Override
//    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
//        Log.d(TAG, "isRelevantUIEvent");
//        if (ev.getObject() instanceof ParseTask) {
//            ParseTask task = (ParseTask)ev.getObject();
//
//            Log.d(TAG, "isRelevantUIEvent yes");
//
//            if (task.isStaticTask()) {
//                if (task.isPending()) {
//                    Log.d(TAG, "isRelevantUIEvent addItem");
//                    getAdapter().addItem(task);
//                } else {
//                    Log.d(TAG, "isRelevantUIEvent addItem");
//                    getAdapter().removeItem(task);
//                }
//            }
//        }
//        return false;
//    }


}
