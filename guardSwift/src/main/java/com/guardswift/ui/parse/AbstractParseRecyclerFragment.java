package com.guardswift.ui.parse;

import android.content.Context;
import android.os.Bundle;
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
import com.guardswift.eventbus.events.BootstrapCompleted;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.SlidingPanelActivity;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.guardswift.ui.parse.execution.TaskRecycleAdapter;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.List;

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

    private boolean mLoading = false;
    private boolean mFirstLoad = true;

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
    protected void updatedNetworkQuery() {
        if (GuardSwiftApplication.getInstance().isBootstrapInProgress()) {
            Log.w(TAG, "cancel updatedNetworkQuery because bootstrapping");
            return;
        }

        if (mRecycleView != null) {

            mFirstLoad = true;

            mAdapter = createRecycleAdapter();
            mAdapter.addOnQueryLoadListener(recycleQueryListener);

            reloadAdapter();
        }
    }

    private void reloadAdapter() {
        if (mAdapter != null) {
            if (GuardSwiftApplication.getInstance().isBootstrapInProgress()) {
                Log.w(TAG, "reloadAdapter: application is bootstrapping");
                return;
            }

            if (mLoading) {
                Log.w(TAG, "reloadAdapter: loading is already in progress");
                return;
            }

            mAdapter.loadObjects();
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

        unbinder = ButterKnife.bind(this, rootView);

        mAdapter = createRecycleAdapter();

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecycleView.setLayoutManager(llm);

        mRecycleView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        mRecycleView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                reloadAdapter();
            }
        });

        reloadAdapter();

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


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        if (mAdapter != null) {
            mAdapter.addOnQueryLoadListener(recycleQueryListener);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        if (mAdapter != null) {
            mAdapter.removeOnQueryLoadListener(recycleQueryListener);
        }
        super.onPause();
    }

    private final TaskRecycleAdapter.OnQueryLoadListener<T> recycleQueryListener = new ParseRecyclerQueryAdapter.OnQueryLoadListener<T>() {
        @Override
        public void onLoaded(List<T> objects, Exception e) {
            if (e != null) {
                new HandleException(getActivity(), TAG, "recycleQueryListener", e);
            }

            if (mFirstLoad) {
                mRecycleView.swapAdapter(mAdapter, true);
            }


            mFirstLoad = false;
            mLoading = false;
        }

        @Override
        public void onLoading() {
            mLoading = true;

            if (mRecycleView != null) {
                if (mAdapter.getItems().isEmpty()) {
                    mRecycleView.showProgress();
                } else {
                    mRecycleView.setRefreshing(true);
                }
            }
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }


    public void onEventMainThread(BootstrapCompleted ev) {
        reloadAdapter();
    }

    public void onEventMainThread(UpdateUIEvent ev) {
        if (!mLoading && isRelevantUIEvent(ev)) {
        }
    }

}
