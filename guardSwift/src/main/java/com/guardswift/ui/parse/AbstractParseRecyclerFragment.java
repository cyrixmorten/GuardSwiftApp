package com.guardswift.ui.parse;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.guardswift.ui.parse.execution.TaskRecycleAdapter;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Abstract fragment for displaying recycled ParseObjects
 * <p/>
 * Is currently restricted to a single type of object at a time.
 * <p/>
 * Created by cyrix on 11/15/15.
 */
public abstract class AbstractParseRecyclerFragment<T extends ParseObject, U extends RecyclerView.ViewHolder> extends InjectingFragment {

    protected static final String TAG = AbstractTasksRecycleFragment.class
            .getSimpleName();


    /**
     * Used to get a handle on LDS and update the database contents
     *
     * @return instance of Object being displayed
     */
    protected abstract ExtendedParseObject getObjectInstance();

    /**
     * Factory restricts the query for which LDS is updated and the content being displayed
     *
     * @return ParseQueryAdapter.QueryFactory
     */
    protected abstract ParseQueryAdapter.QueryFactory<T> getNetworkQueryFactory();

    protected abstract ParseRecyclerQueryAdapter<T, U> getRecycleAdapter();

    /**
     * triggered on UI relevant events, return true if this view should refresh contents based on the event
     *
     * @param ev
     * @return
     */
    protected abstract boolean isRelevantUIEvent(UpdateUIEvent ev);

    // block multiple loads on LDS
    private boolean mLoading;
    private ParseRecyclerQueryAdapter<T, U> mAdapter;

    public AbstractParseRecyclerFragment() {
    }


    @Bind(R.id.list)
    protected SuperRecyclerView mRecycleView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void refreshLocalData() {
        mLoading = true;
        if (mAdapter != null) {
            mAdapter.loadObjects();

            Log.e(TAG, "fetch from network");
            getObjectInstance().updateAll(getNetworkQueryFactory().create()).onSuccess(new Continuation<List<T>, Object>() {
                @Override
                public Object then(Task<List<T>> task) throws Exception {
                    Log.e(TAG, "fetch success");
                    if (mAdapter != null) {
                        mAdapter.loadObjects();
                    }
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    if (task.isFaulted()) {
                        new HandleException(TAG, "refreshLocalData", task.getError());
                    }
                    return null;
                }
            });

        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mAdapter != null) {
            mAdapter.onAttatch(getActivity());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mAdapter != null) {
            mAdapter.onDetatch();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recycle_tasks,
                container, false);

        ButterKnife.bind(this, rootView);
        // must be after ButterKnife.bind as it may rely on CoordinatorLayout
        mAdapter = getRecycleAdapter();
        mAdapter.setFromLocalDataStore(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecycleView.setLayoutManager(llm);
        mRecycleView.setAdapter(mAdapter);

        mRecycleView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        mRecycleView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // pull-to-refresh
                refreshLocalData();
            }
        });

        mRecycleView.showProgress();

        // load freshly updated objects when view is resumed
        // delay a bit to allow navigation drawer to close before loading
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLocalData();
            }
        }, 1000);

        return rootView;
    }


    @Override
    public void onResume() {
        if (mAdapter != null) {
            mAdapter.addOnQueryLoadListener(recycleQueryListener);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        if (mAdapter != null)
            mAdapter.removeOnQueryLoadListener(recycleQueryListener);
        super.onPause();
    }

    private final TaskRecycleAdapter.OnQueryLoadListener<T> recycleQueryListener = new ParseRecyclerQueryAdapter.OnQueryLoadListener<T>() {
        @Override
        public void onLoaded(List<T> objects, Exception e) {
            if (e != null) {
                new HandleException(getActivity(), TAG, "recycleQueryListener", e);
            }
            mLoading = false;
            if (mRecycleView != null) {
                mRecycleView.hideProgress();
                mRecycleView.showRecycler();
            }
        }

        @Override
        public void onLoading() {
            mLoading = true;
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    public void onEventMainThread(UpdateUIEvent ev) {
        if (mAdapter != null && !mLoading && isRelevantUIEvent(ev)) {
            mAdapter.loadObjects();
        }
    }

}
