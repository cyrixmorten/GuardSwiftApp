package com.guardswift.ui.parse.documentation.report.create.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.guardswift.persistence.cache.task.DistrictWatchClientCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventLog.EventCodes;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchClient;
import com.guardswift.ui.GuardSwiftApplication;

import javax.inject.Inject;

public class DistrictWatchClientCreateEventHandlerActivity extends
		AbstractCreateEventHandlerActivity {

	private static final String TAG = DistrictWatchClientCreateEventHandlerActivity.class
			.getSimpleName();

	public static void start(Context context, DistrictWatchClient districtWatchClient) {

		GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchClientCache().setSelected(districtWatchClient);

		context.startActivity(new Intent(context, DistrictWatchClientCreateEventHandlerActivity.class));
	}

	@Inject
	DistrictWatchClientCache districtWatchClientCache;

	private DistrictWatchClient mDistrictWatchClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDistrictWatchClient = districtWatchClientCache.getSelected();
	}




//	@Override
//	Client getClient() {
//		return mDistrictWatchClient.getClient();
//	}

	@Override
	void saveEvent(String event, int amount, String people, String clientLocation,
			String remarks) {

        new EventLog.Builder(this)
                .taskPointer(mDistrictWatchClient, GSTask.EVENT_TYPE.OTHER)
                .event(event)
                .amount(amount)
				.people(people)
                .location(clientLocation)
                .remarks(remarks)
                .eventCode(EventCodes.DISTRICTWATCH_OTHER).saveAsync();

//        mDistrictWatchClient.getTaskSummaryInstance().event(eventlog);
	}

}
