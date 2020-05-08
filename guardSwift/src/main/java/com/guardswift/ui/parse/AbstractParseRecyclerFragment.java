package com.guardswift.ui.parse;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.eventbus.events.BootstrapCompleted;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.SlidingPanelActivity;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.guardswift.ui.parse.execution.TaskRecycleAdapter;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.parse.ui.widget.ParseQueryAdapter;

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
public abstract class AbstractParseRecyclerFragment<T extends ExtendedParseObject, U extends RecyclerView.ViewHolder> extends InjectingFragment {

    protected static final String TAG = AbstractTasksRecycleFragment.class
            .getSimpleName();

    private Unbinder unbinder;

    // does a full reload of data on resume if true
    private boolean reloadOnResume = false;

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

    private ParseRecyclerQueryAdapter<T, U> mAdapter;



    @BindView(R.id.list)
    protected SuperRecyclerView mRecycleView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // trigger this when there are changes in the query
    protected void updatedNetworkQuery() {
        if (GuardSwiftApplication.getInstance().isBootstrapInProgress()) {
            Log.w(TAG, "cancel updatedNetworkQuery because bootstrapping");
            return;
        }

        if (mRecycleView != null) {

            mRecycleView.setAdapter(null);
            mAdapter = createRecycleAdapter();
            mAdapter.addOnQueryLoadListener(recycleQueryListener);

            reloadAdapter();
        }
    }

    protected void reloadAdapter() {
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
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (mAdapter != null) {
            mAdapter.onAttach(getActivity());
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        if (mAdapter != null) {
            mAdapter.onDetach();
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_recycle_tasks,
                container, false);

        unbinder = ButterKnife.bind(this, rootView);


        mAdapter = createRecycleAdapter();

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);

        mRecycleView.setLayoutManager(llm);

        mRecycleView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        mRecycleView.setRefreshListener(this::reloadAdapter);

        reloadAdapter();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof SlidingPanelActivity) {
            ((SlidingPanelActivity) getActivity()).setSlidingScrollView(mRecycleView);
        }

    }

    protected ParseRecyclerQueryAdapter<T, U> getAdapter() {
        return mAdapter;
    }

    protected void setReloadOnResume() {
        this.reloadOnResume = true;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume - reloadOnResume: " + reloadOnResume);

        if (mAdapter != null) {
            mAdapter.addOnQueryLoadListener(recycleQueryListener);
            if (reloadOnResume) {
                mAdapter.loadObjects();
            } else {
                mAdapter.notifyDataSetChanged();
            }
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
//            mRecycleView.setRefreshing(false);

            if (mRecycleView.getAdapter() == null) {
                mRecycleView.swapAdapter(mAdapter, true);
            }


            mLoading = false;
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


    public void onEventMainThread(BootstrapCompleted ev) {
        reloadAdapter();
    }

    public void onEventMainThread(UpdateUIEvent ev) {
        if (!mLoading && isRelevantUIEvent(ev)) {
            mAdapter.notifyDataSetChanged();
        }
    }

}
