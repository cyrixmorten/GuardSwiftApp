package com.guardswift.ui.parse.documentation.report.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.view.card.ReportCard;
import com.parse.ui.widget.ParseQueryAdapter;

/**
 * Created by cyrix on 11/21/15.
 */
public class ReportHistoryAdapter extends ParseRecyclerQueryAdapter<Report, ReportHistoryAdapter.ReportViewHolder> {


    private static final String TAG = ReportHistoryAdapter.class.getSimpleName();

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        public ReportCard reportCard;

        public ReportViewHolder(ReportCard reportCard) {
            super(reportCard);

            this.reportCard = reportCard;

        }

    }


    private Context context;

    public ReportHistoryAdapter(Context context, ParseQueryAdapter.QueryFactory<Report> queryFactory) {
        super(queryFactory);
        this.context = context;

    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ReportCard reportCard = new ReportCard(context);
        return new ReportViewHolder(reportCard);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        final Report report = getItem(position);
        Log.d(TAG, "onBindViewHolder: " + report);

        holder.reportCard.setReport(report);

    }
}
