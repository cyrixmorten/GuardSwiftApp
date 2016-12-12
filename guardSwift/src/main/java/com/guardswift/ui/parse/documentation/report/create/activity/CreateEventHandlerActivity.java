package com.guardswift.ui.parse.documentation.report.create.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;

import java.util.Date;

import javax.inject.Inject;

public class CreateEventHandlerActivity extends AbstractCreateEventHandlerActivity {

	private static final String TAG = CreateEventHandlerActivity.class
			.getSimpleName();


	public static void start(Context context, GSTask task) {
		GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().setSelected(task);
		context.startActivity(new Intent(context, CreateEventHandlerActivity.class));
	}

	@Inject
	GSTasksCache taskCache;

	private GSTask task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		task = taskCache.getLastSelected();
		Log.w(TAG, "task: " + task);
	}


//	@Override
//	Client getClient() {
//		return task.getClient();
//	}

	@Override
	void saveEvent(Date timestamp, String event, int amount, String people, String clientLocation,
				   String remarks) {


        new EventLog.Builder(this)
                .taskPointer(task, GSTask.EVENT_TYPE.OTHER)
                .event(event)
                .amount(amount)
				.people(people)
                .location(clientLocation)
                .remarks(remarks)
                .eventCode(task.getEventCode())
				.deviceTimeStamp(timestamp)
				.saveAsync();

//        mAlarm.getTaskSummaryInstance().event(eventlog);
	}

}
