package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.StaticTaskQueryBuilder;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ui.widget.ParseQueryAdapter;

public class FinishedStaticTasksFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = FinishedStaticTasksFragment.class.getSimpleName();


	public static FinishedStaticTasksFragment newInstance() {
		return new FinishedStaticTasksFragment();
	}

	public FinishedStaticTasksFragment() {
        this.setReloadOnResume();
    }


    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return () -> new StaticTaskQueryBuilder(false).
                status(ParseTask.STATUS.FINISHED).
                daysBack(7).
                sortedByUpdated().
                build();
    }

//    @Override
//    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
//        if (ev.getObject() instanceof ParseTask) {
//            ParseTask task = (ParseTask)ev.getObject();
//
//            if (task.isStaticTask()) {
//                if (task.isFinished()) {
//                    getAdapter().addItem(task);
//                } else {
//                    getAdapter().removeItem(task);
//                }
//            }
//        }
//        return false;
//    }


}
