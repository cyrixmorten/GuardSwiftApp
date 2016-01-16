//package com.guardswift.ui.fragments.task.alarm;
//
//import android.app.Activity;
//import android.content.Context;
//import android.location.Location;
//import android.os.Bundle;
//import android.text.InputType;
//import android.text.format.DateUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.beardedhen.androidbootstrap.BootstrapButton;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingFragment;
//import com.guardswift.eventbus.events.UpdateUIEvent;
//import com.guardswift.core.exceptions.HandleException;
//import com.guardswift.persitence.cache.GuardCache;
//import com.guardswift.persitence.cache.task.AlarmCache;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.documentation.eventlog.EventLog;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.guardswift.persistence.parse.planning.GSTask;
//import com.guardswift.ui.view.answer.EditTextFormView;
//import com.guardswift.ui.view.answer.TextViewFormView;
//import com.guardswift.ui.view.card.QuestionCardView;
//import com.parse.ParseObject;
//
//import org.joda.time.DateTime;
//import org.joda.time.Minutes;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//import javax.inject.Inject;
//
//import bolts.Continuation;
//import bolts.Task;
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import de.greenrobot.event.EventBus;
//
//public class AlarmReportFragment extends InjectingFragment {
//
//    protected static final String TAG = AlarmReportFragment.class
//            .getSimpleName();
//
//
//
//    @Inject
//    AlarmCache alarmCache;
//    @Inject
//    GuardCache guardCache;
//
//    private Guard mGuard;
//    private Client mClient;
//    private Alarm mAlarm;
//
//    public static AlarmReportFragment newInstance(Context context, Alarm alarm) {
//
//        new AlarmCache(context).setSelected(alarm);
//
//        AlarmReportFragment fragment = new AlarmReportFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    public AlarmReportFragment() {
//    }
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mGuard = guardCache.getLoggedIn();
//        mAlarm = alarmCache.getSelected();
//        mClient = mAlarm.getClient();
//    }
//
//
//    @Bind(R.id.layout_questions)
//    LinearLayout layout_questions;
//
//    @Bind(R.id.layout_internal)
//    LinearLayout layout_internal;
//
//    private List<QuestionCardView> questionCards = Lists.newArrayList();
//    private List<QuestionCardView> internalCards = Lists.newArrayList();
//
//    private QuestionCardsHolder questionHolder;
//    private AnswerHolder answerHolder;
//
//    private InternalCardsHolder internalCardsHolder;
//    private InternalAnswerHolder internalAnswerHolder;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_alarm_report,
//                container, false);
//
//        ButterKnife.bind(this, rootView);
//
//        createQuestionCards();
//        createInternalCards();
//
//        addAllQuestionsCards();
//        addAllInternalCards();
//
//        return rootView;
//    }
//
//
//    private void createQuestionCards() {
//
//        JSONObject report = mAlarm.getJSONReport();
//        if (report != null) {
//            try {
//                report = report.getJSONObject("report");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        questionHolder = new QuestionCardsHolder(report);
//
//        questionCards = Lists.newArrayList(
//                questionHolder.alarm_triggered,
//                questionHolder.canceled,
//                questionHolder.security_level,
//                questionHolder.zone,
//                questionHolder.group,
//                questionHolder.open_windows,
//                questionHolder.open_doors,
//                questionHolder.signs_of_entry,
//                questionHolder.people_detained,
//                questionHolder.people_ducked,
//                questionHolder.signs_of_abbandon,
//                questionHolder.signs_of_technical_error,
//                questionHolder.remarks,
//                questionHolder.alarm_reset,
//                questionHolder.guard);
//    }
//
//    private void createInternalCards() {
//
//        JSONObject report = mAlarm.getJSONReport();
//        if (report != null) {
//            try {
//                report = report.getJSONObject("internal");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        internalCardsHolder = new InternalCardsHolder(report);
//
//
//        internalCards = Lists.newArrayList(
//                internalCardsHolder.called,
//                internalCardsHolder.drived_towards,
//                internalCardsHolder.arrived,
//                internalCardsHolder.finished,
//                internalCardsHolder.reaction,
//                internalCardsHolder.repairs,
//                internalCardsHolder.info,
//                internalCardsHolder.remarks
//        );
//    }
//
//    @OnClick(R.id.button_create_report)
//    public void createReportButton(BootstrapButton button) {
//        JSONObject report = createReport();
//        mAlarm.put("report", report);
//
//        final MaterialDialog progress_dialog = new MaterialDialog.Builder(getActivity())
//                .title(R.string.saving_report)
//                .content(R.string.please_wait)
//                .progress(true, 0)
//                .show();
//
//        progress_dialog.setCancelable(false);
//        progress_dialog.setCanceledOnTouchOutside(false);
//
////        try {
////            JSONObject clientReport = report.getJSONObject("report");
////
////            final ParseObject alarmReport = new ParseObject("AlarmReport");
////            alarmReport.put("hwId", mAlarm.getHardwareId());
////            alarmReport.put("client", mClient);
////            alarmReport.put("alarm", mAlarm);
////            alarmReport.put("report", clientReport);
////
////            ParseACL acl = new ParseACL();
////            acl.setPublicReadAccess(true);
////            acl.setPublicWriteAccess(false);
////
////            alarmReport.setACL(acl);
//
//
//            EventLog.getQueryBuilder(false).matching(mAlarm).eventCode(EventLog.EventCodes.ALARM_OTHER_REPORT).build().findInBackground().onSuccessTask(new Continuation<List<EventLog>, Task<Void>>() {
//                @Override
//                public Task<Void> then(Task<List<EventLog>> task) throws Exception {
////                    if (task.isFaulted()) {
////                        progress_dialog.dismiss();
////                        new ParseExceptionHandler(getActivity(), task.getError());
////                        return null;
////                    }
//                    List<EventLog> eventLogs = task.getResult();
//                    Log.d(TAG, "Deleting " + eventLogs.size() + " previous events");
//                    return ParseObject.deleteAllInBackground(task.getResult());
//                }
//            }).onSuccessTask(new Continuation<Void, Task<Void>>() {
//                @Override
//                public Task<Void> then(Task<Void> task) throws Exception {
////                    if (task.isFaulted()) {
////                        progress_dialog.dismiss();
////                        new ParseExceptionHandler(getActivity(), task.getError());
////                        return null;
////                    }
//                    Log.d(TAG, "Saving new/edited events");
//                    return ParseObject.saveAllInBackground(createEventLogs());
//                }
//            }).onSuccessTask(new Continuation<Void, Task<Void>>() {
//                @Override
//                public Task<Void> then(Task<Void> task) throws Exception {
////                    if (task.isFaulted()) {
////                        progress_dialog.dismiss();
////                        new ParseExceptionHandler(getActivity(), task.getError());
////                        return null;
////                    }
//                    Log.d(TAG, "And finally .. saveAsync alarm with updated report");
//                    mAlarm.setSecurityLevel(answerHolder.security_level.getAnswer());
//                    return mAlarm.saveInBackground();
//                }
//            }).continueWith(new Continuation<Void, Object>() {
//                @Override
//                public Object then(Task<Void> task) throws Exception {
//
//                    progress_dialog.dismiss();
//
//                    if (task.isFaulted()) {
//                        Log.e(TAG, "creating reportfailed", task.getError());
//                        new HandleException(getActivity(), TAG,  "creating reportfailed", task.getError());
//                        return null;
//                    }
//
//                    Log.d(TAG, "Done, show report saved dialog");
//
//                    if (getActivity() == null) {
//                        Log.e(TAG, "User has left Activity - aborting");
//                        return null;
//                    }
//
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                                    .title(R.string.report_saved)
//                                    .content(R.string.report_client_access_info, mAlarm.getObjectId())
//                                    .positiveText(android.R.string.ok)
//                                    .show();
//
//                            dialog.setCancelable(false);
//                            dialog.setCanceledOnTouchOutside(false);
//                        }
//                    });
//
//                    return null;
//                }
//            });
//
////            ParseObject.saveAllInBackground(eventLogs, new SaveCallback() {
////                @Override
////                public void done(ParseException e) {
////                    if (e != null) {
////                        new ParseExceptionHandler(getActivity(), e);
////                        progress_dialog.dismiss();
////                        return;
////                    }
////
////                    mAlarm.saveInBackground(new SaveCallback() {
////                        @Override
////                        public void done(ParseException e) {
////                            progress_dialog.dismiss();
////                            if (e != null) {
////                                new ParseExceptionHandler(getActivity(), e);
////                                return;
////                            }
////
////                            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
////                                    .title(R.string.report_saved)
////                                    .content(R.string.report_client_access_info, mAlarm.getObjectId())
////                                    .positiveText(android.R.string.ok)
////                                    .show();
////
////                            dialog.setCancelable(false);
////                            dialog.setCanceledOnTouchOutside(false);
////
////                        }
////                    });
////
////                }
////            });
//
//
////        } catch (JSONException e) {
////            Toast.makeText(getActivity(), getString(R.string.error_an_error_occured), Toast.LENGTH_LONG).show();
////            return;
////        }
//
//    }
//
//
//
//    private List<EventLog> createEventLogs() {
//
//        List<EventLog> eventLogs = Lists.newArrayList();
//
//        boolean canceled = questionHolder.canceled.getAnswer();
//        if (canceled) {
//            String event = questionHolder.canceled.getQuestion();
//            eventLogs.addUnique(createEventLog(event, "0", "", ""));
//        }
//
//        boolean open_windows = questionHolder.open_windows.getAnswer();
//        if (open_windows) {
//            String event = questionHolder.open_windows.getQuestion();
//            String amount = answerHolder.open_windows_amount.getAnswer();
//            String location = answerHolder.open_windows_location.getAnswer();
//            eventLogs.addUnique(createEventLog(event, amount, location, ""));
//        }
//
//        boolean open_doors = questionHolder.open_doors.getAnswer();
//        if (open_doors) {
//            String event = questionHolder.open_doors.getQuestion();
//            String amount = answerHolder.open_doors_amount.getAnswer();
//            String location = answerHolder.open_doors_location.getAnswer();
//            eventLogs.addUnique(createEventLog(event, amount, location, ""));
//        }
//
//        boolean signs_of_entry = questionHolder.signs_of_entry.getAnswer();
//        if (signs_of_entry) {
//            String event = questionHolder.signs_of_entry.getQuestion();
//            String remarks = answerHolder.signs_of_entry.getAnswer();
//            eventLogs.addUnique(createEventLog(event, "", "", remarks));
//        }
//
//        boolean people_detained = questionHolder.people_detained.getAnswer();
//        if (people_detained) {
//            String event = questionHolder.people_detained.getQuestion();
//            String amount = answerHolder.people_detained_amount.getAnswer();
//            eventLogs.addUnique(createEventLog(event, amount, "", ""));
//        }
//
//        boolean people_ducked = questionHolder.people_ducked.getAnswer();
//        if (people_ducked) {
//            String event = questionHolder.people_ducked.getQuestion();
//            String amount = answerHolder.people_ducked_amount.getAnswer();
//            eventLogs.addUnique(createEventLog(event, amount, "", ""));
//        }
//
//        boolean signs_of_abbandon = questionHolder.signs_of_abbandon.getAnswer();
//        if (signs_of_abbandon) {
//            String event = questionHolder.signs_of_abbandon.getQuestion();
//            String remarks = answerHolder.signs_of_abbandon.getAnswer();
//            eventLogs.addUnique(createEventLog(event, "", "", remarks));
//        }
//
//        boolean signs_of_technical_error = questionHolder.signs_of_technical_error.getAnswer();
//        if (signs_of_technical_error) {
//            String event = questionHolder.signs_of_technical_error.getQuestion();
//            String remarks = answerHolder.signs_of_technical_error.getAnswer();
//            eventLogs.addUnique(createEventLog(event, "", "", remarks));
//        }
//
//        boolean alarm_reset = questionHolder.alarm_reset.getAnswer();
//        if (!alarm_reset) {
//            String event = getString(R.string.alarm_not_reset);
//            String remarks = answerHolder.alarm_reset.getAnswer();
//            eventLogs.addUnique(createEventLog(event, "", "", remarks));
//        }
//
//        String remarks = answerHolder.remarks.getAnswer();
//        if (!remarks.isEmpty()) {
//            String event = getString(R.string.title_event_remarks);
//            eventLogs.addUnique(createEventLog(event, "", "", remarks));
//        }
//
//        return eventLogs;
//
//    }
//
//    private EventLog createEventLog(String event, String amountString, String location, String remarks) {
//        int amount = 0;
//        try {
//            amount = Integer.parseInt(amountString);
//        } catch (NumberFormatException e) {
//            amount = 0;
//        }
//
//        return new EventLog.Builder(getActivity())
//                .event(event)
//                .amount(amount)
//                .location(location)
//                .remarks(remarks)
//                .taskPointer(mAlarm, GSTask.EVENT_TYPE.OTHER)
//                .eventCode(EventLog.EventCodes.ALARM_OTHER_REPORT).build();
//
//    }
//
//    private JSONObject createReport() {
//
//        Map<String, Object> reportMap = Maps.newHashMap();
//        reportMap.put("date", new Date().getTime());
//
//        reportMap.put("client_number", mClient.getNumber());
//        reportMap.put("client_name", mClient.getName());
//        reportMap.put("client_addressFull", mClient.getFullAddress());
//        reportMap.put("client_addressName", mClient.getAddressName());
//        reportMap.put("client_addressNumber", mClient.getAddressNumber());
//        reportMap.put("client_city", mClient.getCityName());
//
//        reportMap.put("time_triggered", answerHolder.alarmTriggered.getAnswer());
//
//        reportMap.put("canceled", questionHolder.canceled.getAnswer());
//
//        reportMap.put("security_level", answerHolder.security_level.getAnswer());
//        reportMap.put("zone", answerHolder.zone.getAnswer());
//        reportMap.put("group", answerHolder.group.getAnswer());
//
//        Map<String, Object> openwindowsMap = Maps.newHashMap();
//        openwindowsMap.put("answer", questionHolder.open_windows.getAnswer());
//        openwindowsMap.put("amount", answerHolder.open_windows_amount.getAnswer());
//        openwindowsMap.put("location", answerHolder.open_windows_location.getAnswer());
//        reportMap.put("open_windows", openwindowsMap);
//
//        Map<String, Object> opendoorsMap = Maps.newHashMap();
//        opendoorsMap.put("answer", questionHolder.open_doors.getAnswer());
//        opendoorsMap.put("amount", answerHolder.open_doors_amount.getAnswer());
//        opendoorsMap.put("location", answerHolder.open_doors_location.getAnswer());
//        reportMap.put("open_doors", opendoorsMap);
//
//        Map<String, Object> signsofentryMap = Maps.newHashMap();
//        signsofentryMap.put("answer", questionHolder.signs_of_entry.getAnswer());
//        signsofentryMap.put("text", answerHolder.signs_of_entry.getAnswer());
//        reportMap.put("signs_of_entry", signsofentryMap);
//
//        Map<String, Object> peopledetainedMap = Maps.newHashMap();
//        peopledetainedMap.put("answer", questionHolder.people_detained.getAnswer());
//        peopledetainedMap.put("amount", answerHolder.people_detained_amount.getAnswer());
//        reportMap.put("people_detained", peopledetainedMap);
//
//        Map<String, Object> peopleduckedMap = Maps.newHashMap();
//        peopleduckedMap.put("answer", questionHolder.people_ducked.getAnswer());
//        peopleduckedMap.put("amount", answerHolder.people_ducked_amount.getAnswer());
//        reportMap.put("people_ducked", peopleduckedMap);
//
//        Map<String, Object> signsofabbandonMap = Maps.newHashMap();
//        signsofabbandonMap.put("answer", questionHolder.signs_of_abbandon.getAnswer());
//        signsofabbandonMap.put("text", answerHolder.signs_of_abbandon.getAnswer());
//        reportMap.put("signs_of_abbandon", signsofabbandonMap);
//
//        Map<String, Object> signsoftechnicalerrorMap = Maps.newHashMap();
//        signsoftechnicalerrorMap.put("answer", questionHolder.signs_of_technical_error.getAnswer());
//        signsoftechnicalerrorMap.put("text", answerHolder.signs_of_technical_error.getAnswer());
//        reportMap.put("signs_of_technical_error", signsoftechnicalerrorMap);
//
//        reportMap.put("remarks", answerHolder.remarks.getAnswer());
//
//        reportMap.put("guardName", answerHolder.guardName.getAnswer());
//        reportMap.put("guardId", answerHolder.guardId.getAnswer());
//
//        Map<String, Object> alarmresetMap = Maps.newHashMap();
//        alarmresetMap.put("answer", questionHolder.alarm_reset.getAnswer());
//        alarmresetMap.put("text", answerHolder.alarm_reset.getAnswer());
//        reportMap.put("alarm_reset", alarmresetMap);
//
//        Map<String, Object> internalMap = Maps.newHashMap();
//
//        internalMap.put("time_called", internalAnswerHolder.called.getAnswer());
//        internalMap.put("time_drive", internalAnswerHolder.drived_towards.getAnswer());
//        internalMap.put("time_arrived", internalAnswerHolder.arrived.getAnswer());
//        internalMap.put("time_left", internalAnswerHolder.finished.getAnswer());
//        internalMap.put("reaction_minutes", internalAnswerHolder.reaction.getAnswer());
//
//        Map<String, Object> repairMap = Maps.newHashMap();
//        repairMap.put("answer", internalCardsHolder.repairs.getAnswer());
//        internalMap.put("repairs", repairMap);
//
//        internalMap.put("central", internalAnswerHolder.central.getAnswer());
//        internalMap.put("clientNo", internalAnswerHolder.clientNo.getAnswer());
//        internalMap.put("client_phonenumber", internalAnswerHolder.clientPhone.getAnswer());
//        internalMap.put("installer", internalAnswerHolder.installer.getAnswer());
//
//        internalMap.put("remarks", internalAnswerHolder.remarks.getAnswer());
//
//
//        Map<String, Object> completereportMap = Maps.newHashMap();
//
//        completereportMap.put("report", reportMap);
//        completereportMap.put("internal", internalMap);
//
//        return new JSONObject(completereportMap);
//    }
//
//    public void onEventMainThread(UpdateUIEvent ev) {
//        Object obj = ev.getObject();
//        if (obj instanceof Location) {
//            // ignore Location updates
//            return;
//        }
//
//        if (isAdded()) {
//            if (internalAnswerHolder != null)
//                internalAnswerHolder.update();
//        }
//    }
//
//    private void addAllQuestionsCards() {
//        for (QuestionCardView question : questionCards) {
//            layout_questions.addView(question);
//        }
//    }
//
//    private void addAllInternalCards() {
//        for (QuestionCardView internal : internalCards) {
//            layout_internal.addView(internal);
//        }
//    }
//
//    private void addAllQuestionsExcept(int... indexes) {
//        int index = 0;
//        for (QuestionCardView question : questionCards) {
//            boolean skip = false;
//            for (Integer tIndex : indexes) {
//                if (index == tIndex) {
//                    skip = true;
//                }
//            }
//            if (!skip)
//                layout_questions.addView(question);
//            index++;
//        }
//    }
//
//    private void removeAllQuestionsExcept(int... indexes) {
//        int index = 0;
//        for (QuestionCardView question : questionCards) {
//            boolean skip = false;
//            for (Integer tIndex : indexes) {
//                if (index == tIndex) {
//                    skip = true;
//                }
//            }
//            if (!skip)
//                layout_questions.removeView(question);
//            index++;
//        }
//    }
//
//
//    private class QuestionCardsHolder {
//
//        final QuestionCardView alarm_triggered;
//        final QuestionCardView canceled;
//        final QuestionCardView security_level;
//        final QuestionCardView zone;
//        final QuestionCardView group;
//        final QuestionCardView open_windows;
//        final QuestionCardView open_doors;
//        final QuestionCardView signs_of_entry;
//        final QuestionCardView people_detained;
//        final QuestionCardView people_ducked;
//        final QuestionCardView signs_of_abbandon;
//        final QuestionCardView signs_of_technical_error;
//        final QuestionCardView remarks;
//        final QuestionCardView alarm_reset;
//        final QuestionCardView guard;
//
//        private JSONObject report;
//
//
//        public QuestionCardsHolder(JSONObject report) {
//
//            this.report = report;
//
//            answerHolder = new AnswerHolder(report);
//
//            alarm_triggered = new QuestionCardView.Builder(getActivity()).question("Alarm indgået").showButtons(false).addAnswers(
//                    answerHolder.alarmTriggered
//            ).build();
//
//            canceled = new QuestionCardView.Builder(getActivity()).question("Afmeldt under udrykning")
//                    .yesClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            removeAllQuestionsExcept(0, 1);
//                        }
//                    })
//                    .noClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            addAllQuestionsExcept(0, 1);
//                        }
//                    }).build();
//
//            security_level = new QuestionCardView.Builder(getActivity()).showQuestion(false)
//                    .addAnswers(
//                            answerHolder.security_level
//                    ).build(true);
//
//            zone = new QuestionCardView.Builder(getActivity()).showQuestion(false)
//                    .addAnswers(
//                            answerHolder.zone
//                    ).build(true);
//
//            group = new QuestionCardView.Builder(getActivity()).showQuestion(false)
//                    .addAnswers(
//                            answerHolder.group
//                    ).build(true);
//
//            open_windows = new QuestionCardView.Builder(getActivity()).question("Åbne vinduer")
//                    .addAnswers(
//                            answerHolder.open_windows_amount,
//                            answerHolder.open_windows_location
//                    ).build();
//
//            open_doors = new QuestionCardView.Builder(getActivity()).question("Åbne døre")
//                    .addAnswers(
//                            answerHolder.open_doors_amount,
//                            answerHolder.open_doors_location
//                    ).build();
//
//            signs_of_entry = new QuestionCardView.Builder(getActivity()).question("Tegn på indbrud")
//                    .addAnswers(
//                            answerHolder.signs_of_entry
//                    ).build();
//
//            people_detained = new QuestionCardView.Builder(getActivity()).question("Personer tilbageholdt")
//                    .addAnswers(
//                            answerHolder.people_detained_amount
//                    ).build();
//
//            people_ducked = new QuestionCardView.Builder(getActivity()).question("Personer undveget")
//                    .addAnswers(
//                            answerHolder.people_ducked_amount
//                    ).build();
//
//            signs_of_abbandon = new QuestionCardView.Builder(getActivity()).question("Tegn på stedet lige er forladt")
//                    .addAnswers(
//                            answerHolder.signs_of_abbandon
//                    ).build();
//
//            signs_of_technical_error = new QuestionCardView.Builder(getActivity()).question("Tegn på teknisk fejl")
//                    .addAnswers(
//                            answerHolder.signs_of_technical_error
//                    ).build();
//
//            remarks = new QuestionCardView.Builder(getActivity()).showQuestion(false)
//                    .addAnswers(
//                            answerHolder.remarks
//                    ).build(true);
//
//            alarm_reset = new QuestionCardView.Builder(getActivity()).question("Alarm reset").showAnswersOnYesClicked(false)
//                    .addAnswers(
//                            answerHolder.alarm_reset
//                    ).build();
//
//            guard = new QuestionCardView.Builder(getActivity()).question("Vagt").showButtons(false).addAnswers(
//                    answerHolder.guardName,
//                    answerHolder.guardId
//            ).build();
//
//            init();
//        }
//
//        public void init() {
//
//            if (report != null) {
//                try {
////                    Log.d(TAG, report.toString(4));
//                    canceled.setAnswer(report.getBoolean("canceled"));
//                    open_windows.setAnswer(report.getJSONObject("open_windows").getBoolean("answer"));
//                    open_doors.setAnswer(report.getJSONObject("open_doors").getBoolean("answer"));
//                    signs_of_entry.setAnswer(report.getJSONObject("signs_of_entry").getBoolean("answer"));
//                    people_detained.setAnswer(report.getJSONObject("people_detained").getBoolean("answer"));
//                    people_ducked.setAnswer(report.getJSONObject("people_ducked").getBoolean("answer"));
//                    signs_of_abbandon.setAnswer(report.getJSONObject("signs_of_abbandon").getBoolean("answer"));
//                    signs_of_technical_error.setAnswer(report.getJSONObject("signs_of_technical_error").getBoolean("answer"));
//                    alarm_reset.setAnswer(report.getJSONObject("alarm_reset").getBoolean("answer"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                canceled.answerNo();
//                open_windows.answerNo();
//                open_doors.answerNo();
//                signs_of_entry.answerNo();
//                people_detained.answerNo();
//                people_ducked.answerNo();
//                signs_of_abbandon.answerNo();
//                signs_of_technical_error.answerNo();
//                alarm_reset.answerYes();
//            }
//        }
//
//    }
//
//    /*
//     * Holder class for all departure answers
//     * Makes it easy to init values of all answers on model changes
//     */
//    private class AnswerHolder {
//
//        final TextViewFormView alarmTriggered = new TextViewFormView.Builder(getActivity()).largeText().build();
//
//        public EditTextFormView security_level = new EditTextFormView.Builder(getActivity()).description("Sikrings niveau").inputType(InputType.TYPE_CLASS_NUMBER).build();
//        final EditTextFormView zone = new EditTextFormView.Builder(getActivity()).description("Zone").inputType(InputType.TYPE_CLASS_TEXT).build();
//        final EditTextFormView group = new EditTextFormView.Builder(getActivity()).description("Gruppe").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView open_windows_amount = new EditTextFormView.Builder(getActivity()).description("Antal").inputType(InputType.TYPE_CLASS_NUMBER).build();
//        final EditTextFormView open_windows_location = new EditTextFormView.Builder(getActivity()).description("Placering").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView open_doors_amount = new EditTextFormView.Builder(getActivity()).description("Antal").inputType(InputType.TYPE_CLASS_NUMBER).build();
//        final EditTextFormView open_doors_location = new EditTextFormView.Builder(getActivity()).description("Placering").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView signs_of_entry = new EditTextFormView.Builder(getActivity()).description("Hvilke").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView people_detained_amount = new EditTextFormView.Builder(getActivity()).description("Antal").inputType(InputType.TYPE_CLASS_NUMBER).build();
//        final EditTextFormView people_ducked_amount = new EditTextFormView.Builder(getActivity()).description("Antal").inputType(InputType.TYPE_CLASS_NUMBER).build();
//
//        final EditTextFormView signs_of_abbandon = new EditTextFormView.Builder(getActivity()).description("Hvilke").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView signs_of_technical_error = new EditTextFormView.Builder(getActivity()).description("Hvilke").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView remarks = new EditTextFormView.Builder(getActivity()).description("Bemærkninger").allowScrollInsideScrollview().build();
//
//        final EditTextFormView alarm_reset = new EditTextFormView.Builder(getActivity()).description("Hvorfor ikke").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final TextViewFormView guardId = new TextViewFormView.Builder(getActivity()).text(String.valueOf(mGuard.getGuardId())).build();
//        final TextViewFormView guardName = new TextViewFormView.Builder(getActivity()).text(mGuard.getName()).build();
//
//
//        private JSONObject report;
//
//        public AnswerHolder(JSONObject report) {
//            // init when created
//            this.report = report;
//            init();
//        }
//
//        private void init() {
//            Date alarmDate = mAlarm.getAlarmTime();
////            String formattedAlarmDate = DateFormat.getLongDateFormat(getActivity()).format(alarmDate);
//            String formattedAlarmDate = android.text.format.DateUtils.formatDateTime(getActivity(),
//                    alarmDate.getTime(),
//                    DateUtils.FORMAT_SHOW_TIME | android.text.format.DateUtils.FORMAT_SHOW_DATE | android.text.format.DateUtils.FORMAT_SHOW_YEAR);
//            alarmTriggered.setTextValue(formattedAlarmDate, "");
//            if (report != null) {
//                try {
//                    security_level.setFormValue(report.getString("security_level"));
//                    zone.setFormValue(report.getString("zone"));
//                    group.setFormValue(report.getString("group"));
//
//                    open_windows_amount.setFormValue(report.getJSONObject("open_windows").getString("amount"));
//                    open_windows_location.setFormValue(report.getJSONObject("open_windows").getString("location"));
//                    open_doors_amount.setFormValue(report.getJSONObject("open_doors").getString("amount"));
//                    open_doors_location.setFormValue(report.getJSONObject("open_doors").getString("location"));
//                    signs_of_entry.setFormValue(report.getJSONObject("signs_of_entry").getString("text"));
//                    people_detained_amount.setFormValue(report.getJSONObject("people_detained").getString("amount"));
//                    people_ducked_amount.setFormValue(report.getJSONObject("people_ducked").getString("amount"));
//                    signs_of_abbandon.setFormValue(report.getJSONObject("signs_of_abbandon").getString("text"));
//                    signs_of_technical_error.setFormValue(report.getJSONObject("signs_of_technical_error").getString("text"));
//                    remarks.setFormValue(report.getString("remarks"));
//                    alarm_reset.setFormValue(report.getJSONObject("alarm_reset").getString("text"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                security_level.setFormValue(mAlarm.getSecurityLevelString());
//                zone.setFormValue(mAlarm.getZone());
//                group.setFormValue("");
//                open_windows_amount.setFormValue("");
//                open_windows_location.setFormValue("");
//                open_doors_amount.setFormValue("");
//                open_doors_location.setFormValue("");
//                signs_of_entry.setFormValue("");
//                people_detained_amount.setFormValue("");
//                people_ducked_amount.setFormValue("");
//                signs_of_abbandon.setFormValue("");
//                signs_of_technical_error.setFormValue("");
//                remarks.setFormValue("");
//                alarm_reset.setFormValue("");
//            }
//        }
//
//    }
//
//    private class InternalCardsHolder {
//
//
//        final QuestionCardView called;
//        final QuestionCardView drived_towards;
//        final QuestionCardView arrived;
//        final QuestionCardView finished;
//        final QuestionCardView reaction;
//        final QuestionCardView repairs;
//        final QuestionCardView info;
//        final QuestionCardView remarks;
//
//        private JSONObject report;
//
//        public InternalCardsHolder(JSONObject report) {
//
//            this.report = report;
//
//            internalAnswerHolder = new InternalAnswerHolder(report);
//
//            called = new QuestionCardView.Builder(getActivity()).question("Patrulje tilkaldt kl").showButtons(false).addAnswers(
//                    internalAnswerHolder.called
//            ).build();
//
//            drived_towards = new QuestionCardView.Builder(getActivity()).question("Kørt mod stedet kl").showButtons(false).addAnswers(
//                    internalAnswerHolder.drived_towards
//            ).build();
//
//            arrived = new QuestionCardView.Builder(getActivity()).question("Ankomst til stedet kl").showButtons(false).addAnswers(
//                    internalAnswerHolder.arrived
//            ).build();
//
//            finished = new QuestionCardView.Builder(getActivity()).question("Stedet forladt kl").showButtons(false).addAnswers(
//                    internalAnswerHolder.finished
//            ).build();
//
//            reaction = new QuestionCardView.Builder(getActivity()).question("Reaktionstid minutter").showButtons(false).addAnswers(
//                    internalAnswerHolder.reaction
//            ).build();
//
//            repairs = new QuestionCardView.Builder(getActivity()).question("Afdækning/reparation").build();
//
//            info = new QuestionCardView.Builder(getActivity()).showQuestion(false)
//                    .addAnswers(
//                            internalAnswerHolder.central,
//                            internalAnswerHolder.installer,
//                            internalAnswerHolder.clientNo,
//                            internalAnswerHolder.clientPhone
//                    ).build(true);
//
//            remarks = new QuestionCardView.Builder(getActivity()).showQuestion(false)
//                    .addAnswers(
//                            internalAnswerHolder.remarks
//                    ).build(true);
//
//            init();
//        }
//
//        public void init() {
//
//            if (report != null) {
//                try {
//                    repairs.setAnswer(report.getJSONObject("repairs").getBoolean("answer"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                repairs.answerNo();
//            }
//        }
//    }
//
//    private class InternalAnswerHolder {
//
//
//        final TextViewFormView called = new TextViewFormView.Builder(getActivity()).largeText().build();
//        final TextViewFormView drived_towards = new TextViewFormView.Builder(getActivity()).largeText().build();
//        final TextViewFormView arrived = new TextViewFormView.Builder(getActivity()).largeText().build();
//        final TextViewFormView finished = new TextViewFormView.Builder(getActivity()).largeText().build();
//        final TextViewFormView reaction = new TextViewFormView.Builder(getActivity()).largeText().build();
//
//        final EditTextFormView central = new EditTextFormView.Builder(getActivity()).description("Alarmcentral").inputType(InputType.TYPE_CLASS_TEXT).build();
//        final EditTextFormView installer = new EditTextFormView.Builder(getActivity()).description("Inst.firma").inputType(InputType.TYPE_CLASS_TEXT).build();
//        final EditTextFormView clientNo = new EditTextFormView.Builder(getActivity()).description("Kundenr.").inputType(InputType.TYPE_CLASS_TEXT).build();
//        final EditTextFormView clientPhone = new EditTextFormView.Builder(getActivity()).description("Tlf.nr.").inputType(InputType.TYPE_CLASS_TEXT).build();
//
//        final EditTextFormView remarks = new EditTextFormView.Builder(getActivity()).description("Bemærkninger").allowScrollInsideScrollview().build();
//
//        private JSONObject report;
//        public InternalAnswerHolder(JSONObject report) {
//            this.report = report;
//            init();
//            update();
//        }
//
//
//        public void init() {
//            if (report != null) {
//                try {
//                    central.setFormValue(report.getString("central"));
//                    installer.setFormValue(report.getString("installer"));
//                    clientNo.setFormValue(report.getString("clientNo"));
//                    clientPhone.setFormValue(report.getString("client_phonenumber"));
//                    remarks.setFormValue(report.getString("remarks"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                central.setFormValue(mAlarm.getCentralName());
//                installer.setFormValue(mAlarm.getInstaller());
//                clientNo.setFormValue(mAlarm.getHardwareId());
//
//                String clientPhoneNumber = (!mClient.getContactsWithNames().isEmpty()) ? mClient.getContactsWithNames().get(0).getPhoneNumber() : "";
//                clientPhone.setFormValue(clientPhoneNumber);
//
//                remarks.setFormValue("");
//            }
//
//        }
//
//        public void update() {
//            called.textDateHoursMinutes(mAlarm.getAlarmTime(), "");
//            drived_towards.textDateHoursMinutes(mAlarm.getTimeStartedDriving(), getString(R.string.not_accepted));
//            arrived.textDateHoursMinutes(mAlarm.getTimeArrived(), getString(R.string.not_arrived));
//            finished.textDateHoursMinutes(mAlarm.getTimeEnded(), getString(R.string.not_finished));
//            reaction.setTextValue(calculateReactionTime(mAlarm), getString(R.string.unknown));
//        }
//
//        private String calculateReactionTime(Alarm alarm) {
//            if (alarm.has(Alarm.timeStarted)) {
//                DateTime driveStarted = new DateTime(alarm.getTimeStartedDriving());
//                DateTime arrived = new DateTime(alarm.getTimeArrived());
//
//                return String.valueOf(Minutes.minutesBetween(driveStarted, arrived).getMinutes());
//            } else {
//                return "";
//            }
//        }
//
//    }
//
//
////    private void focusNext(View fromView) {
////        if (getActivity() != null) {
////            View nextField = fromView.focusSearch(View.FOCUS_RIGHT);
////            if (nextField != null) {
////                nextField.requestFocus();
////            }
////        }
////    }
//
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        ButterKnife.unbind(this);
//    }
//
//    @Override
//    public void onDetach() {
//        EventBus.getDefault().unregister(this);
//        super.onDetach();
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        EventBus.getDefault().register(this);
//    }
//
//}
