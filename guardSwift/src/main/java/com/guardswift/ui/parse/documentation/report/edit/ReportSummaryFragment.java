package com.guardswift.ui.parse.documentation.report.edit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.cache.task.StaticTaskCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.documentation.report.create.FragmentVisibilityListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportSummaryFragment extends InjectingFragment implements FragmentVisibilityListener {


    private static final String TAG = ReportSummaryFragment.class.getSimpleName();

    public static ReportSummaryFragment newInstance(StaticTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getStaticTaskCache()
                .setSelected(task);

        ReportSummaryFragment fragment = new ReportSummaryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    StaticTaskCache staticTaskCache;

    @Inject
    ParseModule parseModule;

    private StaticTask staticTask;
    private Report report;
    private Client client;
    private List<ClientContact> clientReceivers = Lists.newArrayList();


    @Bind(R.id.cardview)
    CardView card;

    @Bind(R.id.progress_layout)
    RelativeLayout progress_layout;

    @Bind(R.id.tv_title)
    TextView tvTitle;

    @Bind(R.id.tv_date)
    TextView tvDate;

    @Bind(R.id.tv_guard_name)
    TextView tvGuardName;

    @Bind(R.id.tv_guard_id)
    TextView tvGuardId;

    @Bind(R.id.layout_timed_remarks)
    LinearLayout layoutRemarks;

    @Bind(R.id.layout_client_receivers)
    LinearLayout layoutClientReceivers;

    @Bind(R.id.btn_send_report)
    Button btnSendReport;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        staticTask = staticTaskCache.getSelected();
        client = staticTask.getClient();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.gs_card_report_summary, container,
                false);

        ButterKnife.bind(this, rootView);

        update();

        return rootView;
    }

    private void update() {
        Log.w(TAG, "update");
        loadingReport(true);

        staticTask.getTaskReportingStrategy().getReport().onSuccess(new Continuation<Report, Object>() {

            @Override
            public Object then(Task<Report> reportTask) throws Exception {
                report = reportTask.getResult();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateHeader();
                        updateGuard();
                        updateRemarksSummary();
                        updateClientReceivers();
                        loadingReport(false);
                    }
                });
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if (task.getError() != null) {
                    Log.e(TAG, "update", task.getError());
                    new HandleException(getContext(), TAG, "find report matching staticTask", task.getError());
                }
                return null;
            }
        });
    }

    MaterialDialog sendReportDialog = null;

    @OnClick(R.id.btn_send_report)
    public void sendReport(Button button) {
        btnSendReport.setEnabled(false);
        sendReportDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).intermediateProgress(R.string.working, R.string.sending_reprt).show();

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("reportId", report.getObjectId());
        ParseCloud.callFunctionInBackground(ParseModule.FUNCTION_SEND_REPORT, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                if (e != null) {
                    sendReportFailedWithError(e);
                    return;
                }

                sendReportSuccess();

            }
        });


    }

    private void dismissSendReportDialog() {
        if (sendReportDialog != null) {
            sendReportDialog.dismiss();
            sendReportDialog = null;
        }
        if (btnSendReport != null) {
            btnSendReport.setEnabled(true);
        }
    }

    private void sendReportSuccess() {
        staticTask.getController().performAction(TaskController.ACTION.FINISH, staticTask);
        dismissSendReportDialog();
        if (getActivity() != null) {
            new CommonDialogsBuilder.MaterialDialogs(getActivity()).okCancel(R.string.logout, getString(R.string.question_logout_after_sending_report), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    parseModule.logout(getActivity());
                }
            }, new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    getActivity().finish();
                }
            }).show();
        }
    }

    private void sendReportFailedWithError(Exception error) {
        new HandleException(TAG, "sendReportFailedWithError", error);

        dismissSendReportDialog();
        if (getActivity() != null) {
            new CommonDialogsBuilder.MaterialDialogs(getActivity()).infoDialog(R.string.error, error.getMessage()).show();
        }
    }


    private void updateHeader() {
        Log.w(TAG, "updateHeader");
        tvTitle.setText(client.getName());
        Date createdAt = staticTask.getParseObject().getCreatedAt();
        if (createdAt != null) {
            String dateString = DateUtils.formatDateTime(getContext(), createdAt.getTime(), DateUtils.FORMAT_SHOW_DATE);
            tvDate.setText(dateString);
        } else {
            tvDate.setVisibility(View.INVISIBLE);
        }
    }

    private void updateGuard() {
        Log.w(TAG, "updateGuard");
        Guard guard = staticTask.getGuard();
        if (guard != null) {
            tvGuardName.setText(guard.getName());
            tvGuardId.setText(String.valueOf(guard.getGuardId()));
        }
    }

    private void updateRemarksSummary() {
        Log.w(TAG, "updateRemarksSummary");
        EventLog.getQueryBuilder(true).matchingReportId(staticTaskCache.getSelected().getReportId()).whereIsReportEntry().orderByAscendingTimestamp().build().findInBackground(new FindCallback<EventLog>() {
            @Override
            public void done(final List<EventLog> objects, ParseException e) {
                if (!objects.isEmpty() && getActivity() != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            layoutRemarks.removeAllViews();

                            for (final EventLog eventLog : objects) {
                                final View timedRemark = getActivity().getLayoutInflater().inflate(R.layout.gs_view_timed_remark, null);

                                TextView tvTimestamp = ButterKnife.findById(timedRemark, R.id.tv_timestamp);
                                String timeStampString = DateUtils.formatDateTime(getContext(), eventLog.getDeviceTimestamp().getTime(), DateUtils.FORMAT_SHOW_TIME);
                                tvTimestamp.setText(timeStampString);

                                TextView tvRemarks = ButterKnife.findById(timedRemark, R.id.tv_remarks);
                                String remarkString = eventLog.getRemarks();
                                tvRemarks.setText(remarkString);

                                Log.w(TAG, timeStampString + " " + remarkString);

                                layoutRemarks.addView(timedRemark);
                            }


                        }
                    });

                }
            }
        });
    }

    private void updateClientReceivers() {
        Log.w(TAG, "updateClientReceivers");
        clientReceivers = client.getContactsRequestingReport();
        if (!clientReceivers.isEmpty() && getActivity() != null) {
            layoutClientReceivers.removeAllViews();

            for (final ClientContact clientContact : clientReceivers) {
                final TextView tvClientContact = new TextView(getActivity());
                final String text = clientContact.getName() + ": " + clientContact.getEmail();

                tvClientContact.setText(text);

                Log.w(TAG, text);

                layoutClientReceivers.addView(tvClientContact);
            }
        }
    }

    private void loadingReport(final boolean isLoading) {
        Log.w(TAG, "loadingReport " + isLoading);
        card.setVisibility((isLoading) ? View.INVISIBLE : View.VISIBLE);
        progress_layout.setVisibility((isLoading) ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public void fragmentBecameVisible() {
        loadingReport(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 1000);
    }

    @Override
    public void fragmentBecameInvisible() {

    }
}
