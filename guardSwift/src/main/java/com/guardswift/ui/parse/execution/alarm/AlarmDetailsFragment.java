//package com.guardswift.ui.fragments.task.alarm;
//
//import android.app.Activity;
//import android.content.Context;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//
//import com.google.common.collect.Lists;
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingFragment;
//import com.guardswift.eventbus.events.UpdateUIEvent;
//import com.guardswift.persitence.cache.task.AlarmCache;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.planning.alarm.Alarm;
//import com.guardswift.ui.view.answer.TextViewFormView;
//import com.guardswift.ui.view.card.QuestionCardView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import de.greenrobot.event.EventBus;
//
//public class AlarmDetailsFragment extends InjectingFragment {
//
//    protected static final String TAG = AlarmDetailsFragment.class
//            .getSimpleName();
//
//    public static AlarmDetailsFragment newInstance(Context context, Alarm alarm) {
//
//        new AlarmCache(context).setSelected(alarm);
//
//        AlarmDetailsFragment fragment = new AlarmDetailsFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    public AlarmDetailsFragment() {
//    }
//
//    @Inject
//    AlarmCache alarmCache;
//
//    private Client mClient;
//    private Alarm mAlarm;
//    private List<View> mDetailsViews;
//
//
//    @BindView(R.id.detailsContainer)
//    LinearLayout detailsContainer;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        mAlarm = alarmCache.getSelected();
//        mClient = mAlarm.getClient();
//
//        prepareDetailsViews();
//
//        super.onCreate(savedInstanceState);
//    }
//
//    private void prepareDetailsViews() {
//        mDetailsViews = new ArrayList<View>();
//
//        View report_id = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.report_id)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(getString(R.string.grants_client_access_to_report)).build(),
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getObjectId()).largeText().build()
//        ).build();
//
//        View security_level = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.security_level)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getSecurityLevelString()).build()
//        ).build();
//
//        View client = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.title_client)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mClient.getFullAddress()).build(),
//                new TextViewFormView.Builder(getActivity()).text(mClient.getZipcode() + " " + mClient.getCityName()).build(),
//                new TextViewFormView.Builder(getActivity()).text(mClient.getAddressName2()).build()
//        ).build();
//
//
//        QuestionCardView zone = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.zone)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getZone()).build()
//        ).build();
//
//        View driving_guidance = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.driving_guidance)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getDrivingGuidance()).build()
//        ).build();
//
//        View access_route = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.access_route)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getAccessRoute()).build()
//        ).build();
//
//        View installer = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.installer)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getInstaller()).build()
//        ).build();
//
//        View keybox = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.keybox)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getKeyboxLocation()).build()
//        ).build();
//
//        View bypassCode = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.bypass)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getBypassCode()).build()
//        ).build();
//
//        View guardCode = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.guardCode)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getGuardCode()).build()
//        ).build();
//
//        View controlPanel = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.controlPanel)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getControlpanelLocation()).build()
//        ).build();
//
//        View smokeCannon = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.smokecannon)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getSmokecannonLocation()).build()
//        ).build();
//
//        View remarks = new QuestionCardView.Builder(getActivity()).showButtons(false).question(getString(R.string.title_event_remarks)).addAnswers(
//                new TextViewFormView.Builder(getActivity()).text(mAlarm.getRemark()).build()
//        ).build();
//
//        mDetailsViews = Lists.newArrayList(
//                report_id,
//                security_level,
//                client,
//                zone,
//                driving_guidance,
//                access_route,
//                installer,
//                keybox,
//                bypassCode,
//                guardCode,
//                controlPanel,
//                smokeCannon,
//                remarks
//        );
//
////        mDetailsViews.addUnique(createTextView(mClient.getAddressName() + " "
////                + mClient.getAddressNumber()));
////        mDetailsViews.addUnique(createTextView(mClient.getZipcode() + " "
////                + mClient.getCityName()));
////        mDetailsViews.addUnique(createTextView(mClient.getAddressName2()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(mAlarm.getZone()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(mAlarm.getDrivingGuidance()));
////        mDetailsViews.addUnique(createTextView(mAlarm.getAccessRoute()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(getString(R.string.installer) + ":"));
////        mDetailsViews.addUnique(createTextView(mAlarm.getInstaller()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(getString(R.string.keybox) + ": "
////                + mAlarm.getKeyboxLocation()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(getString(R.string.bypass) + ": "
////                + mAlarm.getBypassCode()));
////        mDetailsViews.addUnique(createTextView(mAlarm.getBypassLocation()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(getString(R.string.guardCode) + ": "
////                + mAlarm.getGuardCode()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(getString(R.string.controlPanel) + ":"));
////        mDetailsViews.addUnique(createTextView(mAlarm.getControlpanelLocation()));
////        mDetailsViews.addUnique(createTextView(""));
////        mDetailsViews.addUnique(createTextView(mAlarm.getSmokecannonLocation()));
////        mDetailsViews.addUnique(createTextView(mAlarm.getRemark()));
//    }
//
////    private TextView createTextView(String text) {
////        TextView tv = new TextView(getActivity());
////        tv.setTextAppearance(getActivity(), R.style.LargeBoldText);
////        tv.setText(text);
////        return tv;
////    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_alarm_details,
//                container, false);
//
//        ButterKnife.bind(this, rootView);
//
//
//        addDetailsViews();
//
//        return rootView;
//    }
//
//
//    public void onEventMainThread(UpdateUIEvent ev) {
//    }
//
//
//    private long addDetailsViews() {
//        if (!mDetailsViews.isEmpty())
//            detailsContainer.removeAllViews();
//
//        // long delay = 500;
//        for (View v : mDetailsViews) {
//            addDetailsView(v, 0);
//            // delay += 100;
//        }
//
//        return 0;
//    }
//
//    private void addDetailsView(final View detailView, long delay) {
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                if (detailsContainer != null)
//                    detailsContainer.addView(detailView);
//            }
//        }, delay);
//    }
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
