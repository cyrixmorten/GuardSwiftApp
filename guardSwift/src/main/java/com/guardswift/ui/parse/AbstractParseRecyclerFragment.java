package com.guardswift.ui.parse;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.guardswift.ui.activity.SlidingPanelActivity;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.guardswift.ui.parse.execution.TaskRecycleAdapter;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

    private Unbinder unbinder;


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
    protected abstract ParseQueryAdapter.QueryFactory<T> createNetworkQueryFactory();

    protected abstract ParseRecyclerQueryAdapter<T, U> createRecycleAdapter();

    /**
     * triggered on UI relevant events, return true if this view should refresh contents based on the event
     *
     * @param ev
     * @return
     */
    protected abstract boolean isRelevantUIEvent(UpdateUIEvent ev);

    // block multiple loads on LDS
    private boolean mLoading;
    private boolean mLoadingNetwork;
    private ParseRecyclerQueryAdapter<T, U> mAdapter;

    public AbstractParseRecyclerFragment() {
    }


    @BindView(R.id.list)
    protected SuperRecyclerView mRecycleView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // trigger when query is updated
    protected void reloadLocalData() {
        if (mRecycleView != null) {

            mRecycleView.showProgress();

            mAdapter = createRecycleAdapter();
            mAdapter.setFromLocalDataStore(true);
//            mRecycleView.setAdapter(null);
//            refreshLocalData();
            loadObjectsFromNetwork().onSuccess(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    mRecycleView.swapAdapter(mAdapter, true);

                    mRecycleView.hideProgress();

                    return null;
                }
            });
        }
    }

    private void refreshLocalData() {
        mLoadingNetwork = true;
        mLoading = true;
        if (mAdapter != null) {
            if (mAdapter.getItems().isEmpty()) {
                Log.d(TAG, "show progress");
                mRecycleView.showProgress();
            }

            // Load from LDS
            mAdapter.loadObjects();

            loadObjectsFromNetwork();

        }

    }

    private Task<Object> loadObjectsFromNetwork() {
        Log.e(TAG, "fetch from network");
        return getObjectInstance().updateAll(createNetworkQueryFactory().create(), 100)
                .onSuccess(new Continuation<List<T>, Object>() {
                    @Override
                    public Object then(Task<List<T>> task) throws Exception {
                        Log.d(TAG, "Results: " + task.getResult().size());

                        if (mAdapter != null) {
                            mAdapter.loadObjects();
//                            mAdapter.showResults(task.getResult());
                        }

                        return task.getResult();
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            new HandleException(TAG, "loadObjectsFromNetwork", task.getError());
                        }

                        mLoadingNetwork = false;


                        return null;
                    }
                });
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

        unbinder = ButterKnife.bind(this, rootView);
        // must be after ButterKnife.bind as it may rely on CoordinatorLayout
        mAdapter = createRecycleAdapter();
        mAdapter.setFromLocalDataStore(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecycleView.setLayoutManager(llm);

        mRecycleView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        mRecycleView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // pull-to-refresh
                refreshLocalData();
            }
        });


        // initial load
        refreshLocalData();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof SlidingPanelActivity) {
            ((SlidingPanelActivity) getActivity()).setSlidingScrollView(mRecycleView);
        }
    }

    protected ParseRecyclerQueryAdapter<T, U> getAdapter() {
        return mAdapter;
    }

    protected SuperRecyclerView getRecycleView() {
        return mRecycleView;
    }


    @Override
    public void onResume() {
        if (mAdapter != null) {
            mAdapter.addOnQueryLoadListener(recycleQueryListener);
            mAdapter.loadObjects(); // refresh to sort by query
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

            mLoading = mLoadingNetwork;

            if (mRecycleView != null && mRecycleView.getAdapter() == null && objects != null && ((objects.size() > 0) || !mLoading)) {
                // delay to allow the rycleview to layout
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mRecycleView != null) {
                            mRecycleView.setAdapter(mAdapter);
                        }
                    }
                }, 500);
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

        unbinder.unbind();
    }


    public void onEventMainThread(UpdateUIEvent ev) {
        if (mAdapter != null && !mLoading && isRelevantUIEvent(ev)) {
            mAdapter.loadObjects();
        }
    }

}
