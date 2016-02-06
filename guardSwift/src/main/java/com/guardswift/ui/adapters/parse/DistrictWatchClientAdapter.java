//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.location.Location;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.beardedhen.androidbootstrap.BootstrapButton;
//import com.guardswift.R;
//import com.guardswift.core.tasks.controller.DistrictWatchClientController;
//import com.guardswift.core.tasks.controller.TaskController;
//import com.guardswift.core.ca.LocationModule;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.core.parse.ParseModule.DistanceStrings;
//import com.guardswift.persitence.cache.DistrictWatchStartedCache;
//import com.guardswift.persistence.parse.tasklist.DistrictWatchStarted;
//import com.guardswift.persistence.parse.planning.districtwatch.DistrictWatchClient;
//import com.guardswift.util.Analytics;
//import com.parse.ParseGeoPoint;
//import com.parse.ParseQuery;
//import com.parse.ParseQueryAdapter;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class DistrictWatchClientAdapter extends
//        ParseQueryAdapter<DistrictWatchClient> {
//
//    private static final String TAG = DistrictWatchClientAdapter.class
//            .getSimpleName();
//
//    private static int sortBy = DistrictWatchClient.SORTBY_NEAREST;
//    private static boolean includeArrived = false;
//
//    private DistrictWatchClientController controller;
//    private final boolean only_visited;
//
//    public void setShowArrived(boolean show) {
//        if (show) DistrictWatchClientAdapter.includeArrived = true;
//        else DistrictWatchClientAdapter.includeArrived = false;
//        loadObjects();
//    }
//
//    public void refresh() {
//        setSortBy(sortBy);
//    }
//
//    public int getSortBy() {
//        return sortBy;
//    }
//
//    public void setSortBy(int sortBy) {
//        DistrictWatchClientAdapter.sortBy = sortBy;
//        loadObjects();
//    }
//
//    public DistrictWatchClientAdapter(final Context context, final boolean only_visited) {
//        super(context,
//                new ParseQueryAdapter.QueryFactory<DistrictWatchClient>() {
//
//                    @Override
//                    public ParseQuery<DistrictWatchClient> create() {
//                        DistrictWatchStarted districtWatchStarted = new DistrictWatchStartedCache(context).getSelected();
//                        DistrictWatchClient.QueryBuilder queryBuilder = new DistrictWatchClient.QueryBuilder(
//                                true).matching(districtWatchStarted.getDistrictWatch()).sortBy(sortBy);
//                        if (only_visited) {
//                            queryBuilder.whereVisited();
//                        }
//                        else {
//                            queryBuilder.missingSupervisions();
//                            queryBuilder.whereArrived(false);
//                        }
//
//                        if (includeArrived) {
//                            queryBuilder.whereArrived(true);
//                        }
//
//                        return queryBuilder.build();
//                    }
//                });
//        this.only_visited = only_visited;
//    }
//
//    @Bind(R.id.clientName)
//    TextView clientName;
//    @Bind(R.id.address)
//    TextView address;
//    @Bind(R.id.type)
//    TextView type;
//    @Bind(R.id.distanceValue)
//    TextView distanceValue;
//    @Bind(R.id.distanceType)
//    TextView distanceType;
//    @Bind(R.id.timesArrived)
//    TextView timesArrived;
//
//    @Bind(R.id.button_arrived)
//    BootstrapButton arrived;
//    @Bind(R.id.button_add_event)
//    Button addEvent;
//
//    @Override
//    public View getItemView(DistrictWatchClient districtWatchClient, View v,
//                            ViewGroup parent) {
//
//        if (v == null) {
//            v = View.inflate(getContext(),
//                    R.layout.view_adapter_item_district_watch, null);
//        }
//
//
//        super.getItemView(districtWatchClient, v, parent);
//
//        ButterKnife.bind(this, v);
//
//        clientName.setText(districtWatchClient.getClientName());
//
//        address.setText(districtWatchClient.getFullAddress());
//        type.setText(districtWatchClient.getDistrictWatchType());
//
//        Location deviceLocation = LocationModule.Recent.getLastKnownLocation();
//        ParseGeoPoint targetGeoPoint = districtWatchClient.getPosition();
//        DistanceStrings distanceStrings = ParseModule.distanceBetweenString(
//                deviceLocation, targetGeoPoint);
//        distanceType.setText(distanceStrings.distanceType);
//        distanceValue.setText(distanceStrings.distanceValue);
//
//        arrived.setTag(districtWatchClient);
//        addEvent.setTag(districtWatchClient);
//
//        timesArrived.setText(getContext().getString(R.string.times_supervised,
//                districtWatchClient.getTimesArrived(), districtWatchClient.getSupervisions()));
//
//        if (only_visited || districtWatchClient.isStarted()) {
//            v.setBackground(getContext().getResources().getDrawable(
//                    R.drawable.x_highlight_grey));
//            arrived.setVisibility(View.GONE);
//            addEvent.setVisibility(View.VISIBLE);
//        } else {
//            v.setBackground(getContext().getResources().getDrawable(
//                    R.drawable.highlight_default));
//            arrived.setVisibility(View.VISIBLE);
//            addEvent.setVisibility(View.GONE);
//        }
//
//        if (deviceLocation == null) {
//            arrived.setEnabled(false);
//            addEvent.setEnabled(false);
//        } else {
//            arrived.setEnabled(true);
//            addEvent.setEnabled(true);
//
////            if (GeofencingModule.Recent.getWithinGeofence().contains(districtWatchClient)) {
////                arrived.setType("success");
////            } else {
////                arrived.setType("primary");
////            }
//        }
//
//        testNoArriveButton(districtWatchClient);
//
//
//        return v;
//
//    }
//
//    private void testNoArriveButton(DistrictWatchClient districtWatchClient) {
//        arrived.setVisibility(View.GONE);
//    }
//
//    @OnClick(R.id.button_add_event)
//    public void addEvent(Button addEvent) {
//
//        final DistrictWatchClient districtWatchClient = (DistrictWatchClient) addEvent
//                .getTag();
//
////        DistrictWatchClient.Recent.setSelected(districtWatchClient);
////
////        Intent intent = new Intent(getContext(),
////                DistrictWatchClientCreateEventActivity.class);
////        getContext().startActivity(intent);
//
//        controller.performAction(TaskController.ACTION.OPEN_WRITE_REPORT, districtWatchClient, false);
//
//        Analytics.eventTaskTrend(Analytics.EventAction.CreateEvent, Analytics.EventLabelTask.Overview);
//
//    }
//
//    @OnClick(R.id.button_arrived)
//    public void arrived(Button arrived) {
//
//        final DistrictWatchClient districtWatchClient = (DistrictWatchClient) arrived
//                .getTag();
//
////        localData.saveEventDistrictWatch(
////                districtWatchClient.getDistrictWatchUnit(),
////                districtWatchClient,
////                getContext().getString(R.string.event_arrived),
////                EventLog.EventCodes.DISTRICTWATCH_ARRIVED);
////
////        districtWatchClient.setArrived(true);
////        districtWatchClient.saveEventually();
//
//        controller.performAction(TaskController.ACTION.ARRIVE, districtWatchClient, false);
//
//        loadObjects();
//
//        Analytics.eventTaskAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Missed);
//        Analytics.eventTaskTrend(Analytics.EventAction.Arrival, Analytics.EventLabelTask.Overview);
//    }
//
//    @Override
//    public View getNextPageView(View v, ViewGroup parent) {
//        if (v == null) {
//            v = View.inflate(getContext(), R.layout.view_adapter_item_loadmore,
//                    null);
//        }
//        return v;
//    }
//
//}
