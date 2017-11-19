package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.StaticTaskQueryBuilder;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class PendingStaticTasksFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = PendingStaticTasksFragment.class.getSimpleName();


	public static PendingStaticTasksFragment newInstance() {
		return new PendingStaticTasksFragment();
	}

	public PendingStaticTasksFragment() {
        this.setReloadOnResume(true);
    }




    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new StaticTaskQueryBuilder(false).
                        status(ParseTask.STATUS.PENDING).
                        daysBack(7).
                        sortedByCreated().
                        build();
            }
        };
    }

//    @Override
//    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
//        if (ev.getObject() instanceof ParseTask) {
//            ParseTask task = (ParseTask)ev.getObject();
//
//            if (task.isStaticTask()) {
//                if (task.isPending()) {
//                    getAdapter().addItem(task);
//                } else {
//                    getAdapter().removeItem(task);
//                }
//            }
//        }
//        return false;
//    }


}
