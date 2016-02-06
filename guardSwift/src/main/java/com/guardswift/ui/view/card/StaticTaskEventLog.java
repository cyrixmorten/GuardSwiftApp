package com.guardswift.ui.view.card;

import android.content.Context;

import com.guardswift.persistence.parse.documentation.event.EventLog;

/**
 * Created by cyrix on 4/19/15.
 */
public class StaticTaskEventLog extends EventLogCard {


    private static final String TAG = StaticTaskEventLog.class.getSimpleName();

    public StaticTaskEventLog(Context context) {
        super(context);

        setCopyToReportEnabled(false);
        setDeletable(false);
        setEditable(false);
        setTimestamped(true);
        setHeaderVisibility(GONE);
    }

    @Override
    public void setEventLog(EventLog eventLog) {
        super.setEventLog(eventLog);

        setRemarksVisibility(VISIBLE);
        // always show remarks

    }

}
