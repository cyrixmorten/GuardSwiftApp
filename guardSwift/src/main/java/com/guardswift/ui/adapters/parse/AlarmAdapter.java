//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.location.Location;
//import android.text.format.DateFormat;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.beardedhen.androidbootstrap.BootstrapButton;
//import com.guardswift.R;
//import com.guardswift.core.tasks.controller.AlarmController;
//import com.guardswift.core.ca.LocationModule;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.core.parse.ParseModule.DistanceStrings;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.guardswift.util.Util;
//import com.parse.ParseGeoPoint;
//import com.parse.ParseQueryAdapter;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class AlarmAdapter extends ParseQueryAdapter<Alarm> {
//
//    private static final String TAG = AlarmAdapter.class.getSimpleName();
//
//
//    private final AlarmController controller;
//
//    /**
//     * Constructor used to display currently active circuit units
//     * @param context
//     */
//    public AlarmAdapter(Context context,
//                        QueryFactory<Alarm> queryFactory) {
//        super(context, queryFactory);
//        this.controller = new AlarmController(context);
//    }
//
//    @Bind(R.id.tv_queMsg) TextView queMsg;
//    @Bind(R.id.securityLevel)
//    TextView securityLevel;
//    @Bind(R.id.zone)
//    TextView zone;
//    @Bind(R.id.alarmType)
//    TextView alarmType;
//    @Bind(R.id.clientAddress)
//    TextView clientAddress;
//    @Bind(R.id.clientAddress2)
//    TextView clientAddress2;
//    @Bind(R.id.clientName)
//    TextView clientName;
//    @Bind(R.id.alarmDate)
//    TextView alarmDate;
//    @Bind(R.id.alarmTime)
//    TextView alarmTime;
//    @Bind(R.id.distanceValue)
//    TextView distanceValue;
//    @Bind(R.id.distanceType)
//    TextView distanceType;
//
////    @Bind(R.id.timeIcon)
////    FontAwesomeText timeIcon;
//
//    @Bind(R.id.sharedText)
//    BootstrapButton sharedText;
//    @Bind(R.id.button_forward)
//    BootstrapButton btnForward;
//    @Bind(R.id.button_mark_accepted)
//    BootstrapButton btnAccept;
//    @Bind(R.id.button_mark_arrived)
//    BootstrapButton btnArrived;
//    @Bind(R.id.button_mark_finished)
//    BootstrapButton btnFinished;
//    @Bind(R.id.button_cancel_arrived)
//    BootstrapButton btnAbort;
//    @Bind(R.id.button_add_event)
//    BootstrapButton btnAddEvent;
//
//    @Override
//    public View getItemView(Alarm alarm, View v, ViewGroup parent) {
//
//        if (v == null) {
//            v = View.inflate(getContext(), R.layout.view_adapter_item_alarm,
//                    null);
//        }
//
//        super.getItemView(alarm, v, parent);
//
//        ButterKnife.bind(this, v);
//
//        fillData(alarm);
//
//        addTagToButtons(alarm);
//
//        v.setBackground(getContext().getResources().getDrawable(
//                R.drawable.x_highlight_grey));
////        timeIcon.setTextColor(Color.GRAY);
//
//        queMsg.setVisibility(View.GONE);
//
//        btnForward.setVisibility(View.GONE);
//
//        if (alarm.isAccepted()) {
//            state_accepted();
//        }
//        if (!alarm.isAccepted() || alarm.isAborted()) {
//            state_not_accepted();
//        }
//        if (alarm.isStarted()) {
//            state_arrived();
//        }
//        if (alarm.isClosed()) {
//            state_closed();
////            v.setBackground(getContext().getResources().getDrawable(
////                    R.drawable.x_highlight_grey));
////            timeIcon.setTextColor(Color.BLACK);
//        }
//
//
//        // TODO temporarily removed
////        check_state_shared(v, alarm);
//
////        if (GeofencingModule.Recent.getWithinGeofence().contains(alarm)) {
////            btnArrived.setType("success");
////        } else {
////            btnArrived.setType("primary");
////        }
//
//        return v;
//
//    }
//
//    private void fillData(Alarm alarm) {
//        Client client = alarm.getClient();
//
//        if (client != null) {
//
//            zone.setText(alarm.getZone());
//
//            String address = client.getCityName() + " "
//                    + client.getAddressName() + " " + client.getAddressNumber();
//            clientAddress.setText(address);
//            clientName.setText(client.getName());
//
//            String clientAddress2String = client.getAddressName2();
//            if (!clientAddress2String.isEmpty()) {
//                clientAddress2.setVisibility(View.VISIBLE);
//                clientAddress2.setText(clientAddress2String);
//            } else {
//                clientAddress2.setVisibility(View.GONE);
//            }
//
//            Location deviceLocation = LocationModule.Recent.getLastKnownLocation();
//            ParseGeoPoint targetGeoPoint = alarm.getClient().getPosition();
//            DistanceStrings distanceStrings = ParseModule
//                    .distanceBetweenString(deviceLocation, targetGeoPoint);
//            distanceType.setText(distanceStrings.distanceType);
//            distanceValue.setText(distanceStrings.distanceValue);
//
//            if (deviceLocation == null) {
//                state_disabled();
//            } else {
//                state_enabled();
//            }
//
//        } else {
//            Log.e(TAG, "client is null");
//        }
//
//        securityLevel.setText(getContext().getString(R.string.security_level_val, alarm.getSecurityLevelString()));
//        alarmType.setText(alarm.getType());
//
//        alarmDate.setText(DateFormat.getDateFormat(getContext()).format(alarm.getAlarmTime()));
//        alarmTime.setText(DateFormat.getTimeFormat(getContext()).format(alarm.getAlarmTime()));
//
//    }
//
//
//
//
//    private void addTagToButtons(Alarm alarm) {
//        btnForward.setTag(alarm);
//        btnAccept.setTag(alarm);
//        btnArrived.setTag(alarm);
//        btnFinished.setTag(alarm);
//        btnAbort.setTag(alarm);
//        btnAddEvent.setTag(alarm);
//
//    }
//
//    private void state_enabled() {
//        btnAccept.setEnabled(true);
//        btnArrived.setEnabled(true);
//        btnFinished.setEnabled(true);
//        btnAbort.setEnabled(true);
//        btnAddEvent.setEnabled(true);
//    }
//
//    private void state_disabled() {
//        btnAccept.setEnabled(false);
//        btnArrived.setEnabled(false);
//        btnFinished.setEnabled(false);
//        btnAbort.setEnabled(false);
//        btnAddEvent.setEnabled(false);
//    }
//
//    private void state_not_accepted() {
//        btnAccept.setVisibility(View.VISIBLE);
//        btnArrived.setVisibility(View.GONE);
//        btnFinished.setVisibility(View.GONE);
//        btnAbort.setVisibility(View.GONE);
//        btnAddEvent.setVisibility(View.GONE);
////        if (preferences.isAlarmResponsible()) {
////            btnForward.setVisibility(View.VISIBLE);
////        }
//    }
//
//    private void state_accepted() {
//        btnAccept.setVisibility(View.GONE);
//        btnArrived.setVisibility(View.VISIBLE);
//        btnFinished.setVisibility(View.GONE);
//        btnAbort.setVisibility(View.GONE);
//        btnAddEvent.setVisibility(View.GONE);
//    }
//
//    private void state_arrived() {
//        btnAccept.setVisibility(View.GONE);
//        btnArrived.setVisibility(View.GONE);
//        btnFinished.setVisibility(View.VISIBLE);
//        btnAbort.setVisibility(View.GONE);
//        btnAddEvent.setVisibility(View.VISIBLE);
//    }
//
//    private void state_closed() {
//        btnAccept.setVisibility(View.GONE);
//        btnArrived.setVisibility(View.GONE);
//        btnFinished.setVisibility(View.GONE);
//        btnAbort.setVisibility(View.GONE);
//        btnAddEvent.setVisibility(View.VISIBLE);
//    }
//
//    private void hide_buttons() {
//        btnAccept.setVisibility(View.GONE);
//        btnArrived.setVisibility(View.GONE);
//        btnFinished.setVisibility(View.GONE);
//        btnAbort.setVisibility(View.GONE);
//        btnAddEvent.setVisibility(View.GONE);
//    }
//
//    private void check_state_shared(View v, Alarm alarm, Guard guard) {
//
//        sharedText.setVisibility(View.GONE);
//
//        if (!alarm.takenByAnyGuard() || alarm.isAborted()) {
//            // not marked by any guard or aborted
//            return;
//        }
//
//        boolean takenByOtherGuard = alarm.takenByAnotherGuard(guard);
//
//        if (!takenByOtherGuard) {
//            // marked by current guard
//            return;
//        }
//
//        if (takenByOtherGuard) {
//            sharedText.setVisibility(View.VISIBLE);
//
//            if (alarm.isAccepted()) {
//                sharedText.setText(getContext().getString(
//                        R.string.marked_alarm_accepted,
//                        alarm.getGuardName(),
//                        Util.dateFormatHourMinutes().format(
//                                alarm.getUpdatedAt())));
//                sharedText.setType("inverse");
//            }
//            if (alarm.isStarted()) {
//                sharedText.setText(getContext().getString(
//                        R.string.marked_arrived,
//                        alarm.getGuardName(),
//                        Util.dateFormatHourMinutes().format(
//                                alarm.getTimeArrived())));
//                sharedText.setType("warning");
//            }
//            if (alarm.isClosed()) {
//                sharedText.setText(getContext().getString(
//                        R.string.marked_finished,
//                        alarm.getGuardName(),
//                        Util.dateFormatHourMinutes().format(
//                                alarm.getTimeEnded())));
//                sharedText.setType("warning");
//            }
//
//            hide_buttons();
//
//        }
//    }
//
//    @OnClick(R.id.button_forward)
//    public void forwardAlarm(BootstrapButton button) {
//        Alarm alarm = (Alarm) button.getTag();
//        controller.performAction(AlarmController.ACTION.FORWARD, alarm, false);
//    }
//
//    @OnClick(R.id.button_add_event)
//    public void addEvent(BootstrapButton button) {
//        Alarm alarm = (Alarm) button.getTag();
//        controller.performAction(AlarmController.ACTION.OPEN_WRITE_REPORT, alarm, false);
//    }
//
//
//    @OnClick(R.id.button_cancel_arrived)
//    public void abortArrived(BootstrapButton button) {
//        Alarm alarm = (Alarm) button.getTag();
//        controller.performAction(AlarmController.ACTION.ABORT, alarm, false);
//    }
//
//    @OnClick(R.id.button_mark_accepted)
//    public void markAccepted(BootstrapButton button) {
//        Alarm alarm = (Alarm) button.getTag();
//        controller.performAction(AlarmController.ACTION.ACCEPT, alarm, false);
//    }
//
//    @OnClick(R.id.button_mark_arrived)
//    public void markArrived(BootstrapButton button) {
//        Alarm alarm = (Alarm) button.getTag();
//        controller.performAction(AlarmController.ACTION.ARRIVE, alarm, false);
//    }
//
//    @OnClick(R.id.button_mark_finished)
//    public void markFinished(BootstrapButton button) {
//        Alarm alarm = (Alarm) button.getTag();
//        controller.performAction(AlarmController.ACTION.FINISH, alarm, false);
//    }
//
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
