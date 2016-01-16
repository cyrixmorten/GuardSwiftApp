//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.location.Location;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.guardswift.R;
//import com.guardswift.core.ca.LocationModule;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.core.parse.ParseModule.DistanceStrings;
//import com.guardswift.persitence.cache.task.GSTasksCache;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.planning.regular.CircuitUnit;
//import com.guardswift.persistence.parse.planning.GSTask;
//import com.parse.ParseGeoPoint;
//import com.parse.ParseQueryAdapter;
//
//import java.util.Set;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class CircuitUnitsAdapter extends ParseQueryAdapter<CircuitUnit> {
//
//    private static final String TAG = CircuitUnitsAdapter.class.getSimpleName();
//
//
//    private final GSTasksCache tasksCache;
//
//    /**
//     * Constructor used to display circuit units
//     */
//    public CircuitUnitsAdapter(Context context, QueryFactory<CircuitUnit> queryFactory) {
//        super(context, queryFactory);
//        this.tasksCache = new GSTasksCache(context);
//    }
//
//    @Bind(R.id.linearLayout_times)
//    LinearLayout timesLayout;
//
//    @Bind(R.id.taskTypeDesc)
//    TextView taskTypeDesc;
//    @Bind(R.id.clientAddress)
//    TextView clientAddress;
//    @Bind(R.id.clientName)
//    TextView clientName;
//    @Bind(R.id.clientNumber)
//    TextView clientNumber;
//    @Bind(R.id.timeStart)
//    TextView timeStart;
//    @Bind(R.id.timeEnd)
//    TextView timeEnd;
//    @Bind(R.id.distanceValue)
//    TextView distanceValue;
//    @Bind(R.id.distanceType)
//    TextView distanceType;
//
//
//    @Override
//    public View getItemView(CircuitUnit circuitUnit, View v, ViewGroup parent) {
//
//        if (v == null) {
//            v = View.inflate(getContext(),
//                    R.layout.gs_card_task, null);
//        }
//
//        super.getItemView(circuitUnit, v, parent);
//
//        ButterKnife.bind(this, v);
//
//        Client client = circuitUnit.getClient();
//        if (client != null) {
//            clientName.setText(client.getName());
//            clientNumber.setText(client.getNumberString());
////            String address = client.getCityName() + " " + client.getAddressName()
////                    + " " + client.getAddressNumber();
//            clientAddress.setText(client.getFullAddress());
//        }
//        taskTypeDesc.setText(circuitUnit.getName());
//        timeStart.setText(circuitUnit.getTimeStartString());
//        timeEnd.setText(circuitUnit.getTimeEndString());
//
//
////        v.setBackground(getContext().getResources().getDrawable(
////                R.drawable.x_highlight_grey));
////
////
////        if (circuitUnit.isExtra()) {
////            v.setBackground(getContext().getResources().getDrawable(
////                    R.drawable.x_highlight_green));
////        }
////
////        if (circuitUnit.isRaid()) {
////            v.setBackground(getContext().getResources().getDrawable(
////                    R.drawable.x_highlight_red));
////        }
//
//        setDistanceValue(circuitUnit, LocationModule.Recent.getLastKnownLocation());
//
//
//
//        return v;
//
//    }
//
//    private void setDistanceValue(GSTask task, Location deviceLocation) {
//
//        if (deviceLocation == null) {
//            return;
//        }
//
//        ParseGeoPoint targetGeoPoint = task.getPosition();
//        DistanceStrings distanceStrings = ParseModule.distanceBetweenString(
//                deviceLocation, targetGeoPoint);
//        distanceType.setText(distanceStrings.distanceType);
//        distanceValue.setText(distanceStrings.distanceValue);
//
//
//        Set<GSTask> allGeofenced = tasksCache.getAllGeofencedTasks();
//        Set<GSTask> within = tasksCache.getWithinGeofence();
//        Set<GSTask> outside = tasksCache.getOutsideGeofence();
//
//        if (within.contains(task)) {
//            setDistanceColor(R.color.button_success_gradient_dark);
//        } else if (outside.contains(task)) {
//            setDistanceColor(R.color.button_warning_gradient_dark);
//        } else if (allGeofenced.contains(task)) {
//            setDistanceColor(R.color.button_info_gradient_dark);
//        } else {
//            setDistanceColor(R.color.button_inverse_disabled);
//        }
//
//        if (timesLayout.getVisibility() == View.GONE) {
//            timesLayout.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void setDistanceColor(int color) {
//        int colorRes = getContext().getResources().getColor(color);
//        distanceType.setTextColor(colorRes);
//        distanceValue.setTextColor(colorRes);
//    }
//
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
