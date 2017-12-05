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
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.cache.task.TaskCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.documentation.report.create.FragmentVisibilityListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ReportSummaryFragment extends InjectingFragment implements FragmentVisibilityListener {


    private static final String TAG = ReportSummaryFragment.class.getSimpleName();

    public static ReportSummaryFragment newInstance(ParseTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache()
                .setSelected(task);

        ReportSummaryFragment fragment = new ReportSummaryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    TaskCache taskCache;

    @Inject
    ParseModule parseModule;

    private ParseTask task;
    private Report report;
    private Client client;


    @BindView(R.id.cardview)
    CardView card;

    @BindView(R.id.progress_layout)
    RelativeLayout progress_layout;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.tv_date)
    TextView tvDate;

    @BindView(R.id.tv_guard_name)
    TextView tvGuardName;

    @BindView(R.id.tv_guard_id)
    TextView tvGuardId;

    @BindView(R.id.layout_timed_remarks)
    LinearLayout layoutRemarks;

    @BindView(R.id.layout_client_receivers)
    LinearLayout layoutClientReceivers;

    @BindView(R.id.btn_send_report)
    Button btnSendReport;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task = taskCache.getSelected();
        client = task.getClient();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.gs_card_report_summary, container,
                false);

        unbinder = ButterKnife.bind(this, rootView);

        update();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    private void update() {
        Log.w(TAG, "update");
        loadingReport(true);


        task.findReport(false).onSuccess(new Continuation<Report, Object>() {

            @Override
            public Object then(Task<Report> reportTask) throws Exception {
                report = reportTask.getResult();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            updateHeader();
                            updateGuard();
                            updateRemarksSummary();
                            updateClientReceivers();
                            loadingReport(false);
                        }
                    }
                });
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if (task.getError() != null) {
                    Log.e(TAG, "update", task.getError());
                    new HandleException(getContext(), TAG, "find report matching task", task.getError());
                }
                return null;
            }
        });
    }

    MaterialDialog sendReportDialog = null;

    @OnClick(R.id.btn_send_report)
    public void sendReport(Button button) {
        btnSendReport.setEnabled(false);
        sendReportDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).indeterminate(R.string.working, R.string.sending_reprt).show();

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
        task.getController().performAction(TaskController.ACTION.FINISH, task);
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
        Date createdAt = task.getParseObject().getCreatedAt();
        if (createdAt != null) {
            String dateString = DateUtils.formatDateTime(getContext(), createdAt.getTime(), DateUtils.FORMAT_SHOW_DATE);
            tvDate.setText(dateString);
        } else {
            tvDate.setVisibility(View.INVISIBLE);
        }
    }

    private void updateGuard() {
        Log.w(TAG, "updateGuard");
        Guard guard = task.getGuard();
        if (guard != null) {
            tvGuardName.setText(guard.getName());
            tvGuardId.setText(String.valueOf(guard.getGuardId()));
        }
    }

    private void updateRemarksSummary() {
        Log.w(TAG, "updateRemarksSummary");
        EventLog.getQueryBuilder(true).matchingReportId(taskCache.getSelected().getReportId()).whereIsReportEntry().orderByAscendingTimestamp().build().findInBackground(new FindCallback<EventLog>() {
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
        List<ClientContact> clientReceivers = client.getContactsRequestingReport();
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
