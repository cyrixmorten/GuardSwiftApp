package com.guardswift.core.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.guardswift.dagger.InjectingBroadcastReceiver;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseObjectFactory;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class ParseUpdateReceiver extends InjectingBroadcastReceiver {
	private static final String TAG = "ParseUpdateReceiver";

	@Inject ParseObjectFactory parseObjectFactory;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		try {
			JSONObject json = new JSONObject(intent.getExtras().getString(
					"com.parse.Data"));

			String pin = json.getString("pin");
			JSONObject jsonObject = json.getJSONObject("object");

			Log.e(TAG, "!! -- ParseUpdateReceiver -- !!");
			Log.d(TAG, jsonObject.toString());

			ExtendedParseObject parseObject = translatePinToParseObject(pin);

			if (parseObject != null) {
				Log.d(TAG, "translatePin found: " + parseObject.getPin());
				parseObject.updateFromJSON(context, jsonObject);
			}

		} catch (JSONException e) {
			Log.d(TAG, "JSONException: " + e.getMessage());
		}
	}

	private ExtendedParseObject translatePinToParseObject(String pin) {
		for (ExtendedParseObject object : parseObjectFactory.getAll()) {
			if (pin.equals(object.getPin())) {
				return object;
			}
		}
		return null;
	}
}
