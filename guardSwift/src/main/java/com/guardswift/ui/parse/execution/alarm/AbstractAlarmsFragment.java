//package com.guardswift.ui.fragments.task.alarm;
//
//import android.location.Location;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import com.guardswift.R;
//import com.guardswift.ui.adapters.parse.AlarmAdapter;
//import com.guardswift.core.tasks.controller.AlarmController;
//import com.guardswift.dagger.InjectingListFragment;
//import com.guardswift.eventbus.events.UpdateUIEvent;
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.ParseObjectFactory;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.parse.ParseException;
//import com.parse.ParseQuery;
//import com.parse.ParseQueryAdapter;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public abstract class AbstractAlarmsFragment extends InjectingListFragment
//        implements OnRefreshListener {
//
//    protected static final String TAG = AbstractAlarmsFragment.class
//            .getSimpleName();
//
//
//    public AbstractAlarmsFragment() {
//    }
//
//    public abstract ParseQuery<Alarm> getQueryNetwork();
//
//    public abstract void doneLoadingObjects();
//
//
//    protected ParseQuery<Alarm> getQueryLocal() {
//        return getQueryNetwork().fromLocalDatastore();
//    }
//
//
//    @Inject
//    ParseObjectFactory parseObjectFactory;
//    @Inject
//    AlarmController controller;
//
//
//    @Bind(R.id.swipe_container)
//    SwipeRefreshLayout mSwipeRefreshLayout;
//
//    private AlarmAdapter mAdapter;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mAdapter = new AlarmAdapter(getActivity(),
//                new ParseQueryAdapter.QueryFactory<Alarm>() {
//
//                    @Override
//                    public ParseQuery<Alarm> create() {
//                        return getQueryLocal().addDescendingOrder(
//                                ExtendedParseObject.createdAt);
//                    }
//                });
//        mAdapter.setAutoload(false);
//
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View rootView = inflater.inflate(R.layout.listview_swipeable,
//                container, false);
//
//        ButterKnife.bind(this, rootView);
//
//        setListAdapter(mAdapter);
//
//        mSwipeRefreshLayout.setOnRefreshListener(this);
//        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
//
//
//
//        return rootView;
//    }
//
//    @Override
//    public void onListItemClick(ListView l, View v, int clientPosition, long id) {
//        Alarm alarm = mAdapter.getItem(clientPosition);
//        // go to details view
//        controller.performAction(AlarmController.ACTION.OPEN, alarm, false);
//        super.onListItemClick(l, v, clientPosition, id);
//    }
//
//    private void refreshParseData() {
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                mSwipeRefreshLayout.setRefreshing(true);
//            }
//        }, 200);
//
//        parseObjectFactory.getAlarm().updateAll(getQueryNetwork(),
//                new ExtendedParseObject.DataStoreCallback<Alarm>() {
//
//                    @Override
//                    public void success(List<Alarm> objects) {
//                        clearLoadIndicators();
//                        mAdapter.loadObjects();
//
//                        doneLoadingObjects();
//                    }
//
//                    @Override
//                    public void failed(ParseException e) {
//                        if (getActivity() != null) {
//                            clearLoadIndicators();
//                            Toast.makeText(getActivity(),
//                                    "synkronisering fejlede " + e.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
//
//    private void clearLoadIndicators() {
//        if (mSwipeRefreshLayout != null)
//            mSwipeRefreshLayout.setRefreshing(false);
//    }
//
//    @Override
//    public void onRefresh() {
//        refreshParseData();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        ButterKnife.unbind(this);
//    }
//
//
//    @Override
//    public void onEventMainThread(UpdateUIEvent ev) {
//        Object obj = ev.getObject();
//        if (!(obj instanceof Location || obj instanceof Alarm)) {
//            return;
//        }
//
//        Log.e(TAG, "UpdateUIEvent " + (mAdapter != null) + " - " + isAdded());
//        if (mAdapter != null) {
//            mAdapter.loadObjects();
//        }
//    }
//
////    public void onEventMainThread(ParseObjectUpdatedEvent ev) {
////        ExtendedParseObject object = ev.getObject();
////        if (object instanceof Alarm) {
////            EventBus.getDefault().post(new UpdateUIEvent());
////        }
////    }
//
//}
